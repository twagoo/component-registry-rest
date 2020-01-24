package clarin.cmdi.componentregistry.frontend;

import java.security.Principal;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.GroupService;
import clarin.cmdi.componentregistry.IMarshaller;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.OwnerUser;
import clarin.cmdi.componentregistry.RegistrySpace;
import clarin.cmdi.componentregistry.UserCredentials;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.impl.ComponentUtils;
import clarin.cmdi.componentregistry.impl.database.AdminRegistry;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.ComponentDao;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.DefaultNestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.extensions.markup.html.repeater.util.TreeModelProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.dao.DataAccessException;

@SuppressWarnings("serial")
public class AdminHomePage extends SecureAdminWebPage {

    private static final long serialVersionUID = 1L;
    private final static Logger LOG = LoggerFactory.getLogger(AdminHomePage.class);
    private final CMDItemInfo info;
    private AbstractTree tree;
    private transient AdminRegistry adminRegistry = new AdminRegistry();
    @SpringBean(name = "componentRegistryFactory")
    private ComponentRegistryFactory componentRegistryFactory;
    @SpringBean(name = "GroupService")
    private GroupService groupService;
    @SpringBean
    private ComponentDao componentDao;
    @SpringBean
    private IMarshaller marshaller;

    private Component infoView;

    public AdminHomePage(final PageParameters parameters) throws ComponentRegistryException, ItemNotFoundException {
        super(parameters);
        adminRegistry.setComponentRegistryFactory(componentRegistryFactory);
        adminRegistry.setComponentDao(componentDao);
        adminRegistry.setMarshaller(marshaller);
        info = new CMDItemInfo(marshaller);
        addLinks();

        final FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);

        add(infoView = new WebMarkupContainer("infoView")
                .add(new Label("name"))
                .add(new Label("id"))
                .add(feedback)
                .add(createPublishDeleteForm())
                .add(createEditForm(feedback))
                .setDefaultModel(new CompoundPropertyModel<>(info))
                .setOutputMarkupId(true));

        try {
            tree = createTree("tree", createDBTreeModel());
            add(tree);

        } catch (UserUnauthorizedException e) {
            LOG.error("Admin: ", e);
            error("Cannot create tree: error = " + e);
        }

    }

    private Form createPublishDeleteForm() throws ItemNotFoundException, ComponentRegistryException {
        final Form<CMDItemInfo> form = new Form<>("actionsForm");
        CompoundPropertyModel model = new CompoundPropertyModel(info);
        form.setModel(model);

        Button deleteButton = new IndicatingAjaxButton("delete", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                CMDItemInfo info = (CMDItemInfo) form.getModelObject();
                info("deleting:" + info.getName());
                Principal userPrincipal = getUserPrincipal();
                try {
                    adminRegistry.delete(info, userPrincipal);
                    info("Item deleted.");
                    reloadTreeModel(info);
                } catch (Exception e) {
                    LOG.error("Admin: ", e);
                    error("Cannot delete: " + info.getName() + "\n error=" + e);
                }
                if (target != null) {
                    target.add(infoView);
                    target.add(tree);
                }
            }

            @Override
            public boolean isEnabled() {
                return info.isDeletable();

            }
        };
        form.add(deleteButton);
        Button undeleteButton = new IndicatingAjaxButton("undelete", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                CMDItemInfo info = (CMDItemInfo) form.getModelObject();
                info("undeleting:" + info.getName());
                try {
                    adminRegistry.undelete(info);
                    info("Item put back.");
                    reloadTreeModel(info);
                } catch (Exception e) {
                    LOG.error("Admin: ", e);
                    error("Cannot undelete: " + info.getName() + "\n error=" + e);
                }
                if (target != null) {
                    target.add(infoView);
                    target.add(tree);
                }
            }

            @Override
            public boolean isEnabled() {
                return info.isUndeletable();
            }
        };
        form.add(undeleteButton);
        return form;
    }

    private Form createEditForm(final FeedbackPanel feedback) throws ItemNotFoundException, ComponentRegistryException {
        final Form<CMDItemInfo> form = new Form<>("form");
        CompoundPropertyModel model = new CompoundPropertyModel(info);
        form.setModel(model);

        form.add(new TextArea("description")
                .add(new DisableOnDeletedBehavior(info))
                .setOutputMarkupId(true)
        );
        form.add(new TextArea("content")
                .add(new DisableOnDeletedBehavior(info))
                .setOutputMarkupId(true)
        );

        CheckBox forceUpdateCheck = new CheckBox("forceUpdate");
        form.add(forceUpdateCheck);

        final Button submitButton = new IndicatingAjaxButton("submit", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                submitEditForm(form, feedback, target, false);
            }
        };
        form.add(submitButton
                .add(new DisableOnDeletedBehavior(info)));

        final Button publishButton = new IndicatingAjaxButton("publish", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                submitEditForm(form, feedback, target, true);
                //reload tree after publish
                try {
                    reloadTreeModel(info);
                    target.add(tree);
                } catch (UserUnauthorizedException ex) {
                    LOG.error("error reloading tree model", ex);
                } catch (ItemNotFoundException ex) {
                    LOG.error("error reloading tree model", ex);
                }
            }

            @Override
            public boolean isEnabled() {
                return super.isEnabled() && !info.isPublished();
            }
        };
        form.add(publishButton
                .add(new DisableOnDeletedBehavior(info)));
        return form;
    }

    private void submitEditForm(Form<?> form, FeedbackPanel feedback, AjaxRequestTarget target, boolean publish) throws DataAccessException {
        CMDItemInfo info = (CMDItemInfo) form.getModelObject();
        Principal userPrincipal = getUserPrincipal();
        info("submitting:" + info.getName() + " id=(" + info.getId() + ")");
        try {
            adminRegistry.submitFile(info, userPrincipal, publish);
            info("submitting done.");

            final BaseDescription newDescr = componentDao.getByCmdId(info.getId());
            info.setDescription(ComponentUtils.toTypeByIdPrefix(newDescr));
            info.setName(newDescr.getName());
        } catch (Exception e) {
            LOG.error("Admin: ", e);
            error("Cannot submit: " + info.getName() + "\n error=" + e);
        }

        if (target != null) {
            target.add(infoView);
            target.add(feedback);
        }
    }

    private void reloadTreeModel(CMDItemInfo info) throws UserUnauthorizedException, ItemNotFoundException {
        try {
            tree.setModelObject(createDBTreeModel());
        } catch (ComponentRegistryException e) {
            LOG.error("Admin: ", e);
            error("Cannot reload tree: " + info.getName() + "\n error=" + e);
        }
    }

    private AbstractTree createTree(String id, TreeModel treeModel) throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException {
        TreeModelProvider<DefaultMutableTreeNode> provider = new TreeModelProvider<DefaultMutableTreeNode>(treeModel) {
            @Override
            public IModel<DefaultMutableTreeNode> model(DefaultMutableTreeNode object) {
                return Model.of(object);
            }

        };
        final AbstractTree adminTree = new DefaultNestedTree<>(id, provider) {
            @Override
            protected Component newContentComponent(String id, IModel<DefaultMutableTreeNode> nodeModel) {
                if (nodeModel.getObject().isLeaf()) {
                    return new AdminTreeItemLeafNode(id, tree, nodeModel, nodeModel);
                } else {
                    return super.newContentComponent(id, nodeModel);
                }
            }

        };

        adminTree.setOutputMarkupId(true);
        return adminTree;
    }

    private TreeModel createDBTreeModel() throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new DisplayDataNode("ComponentRegistry", false));
        DefaultMutableTreeNode publicNode = new DefaultMutableTreeNode(new DisplayDataNode("Public", false));
        rootNode.add(publicNode);
        ComponentRegistry publicRegistry = componentRegistryFactory.getPublicRegistry();
        add(publicNode, publicRegistry);
        List<ComponentRegistry> userRegistries = componentRegistryFactory.getAllUserRegistries();
        for (ComponentRegistry registry : userRegistries) {
            addRegistry(rootNode, registry, registry.getName());
        }

        final UserCredentials userCredentials = new UserCredentials(getUserPrincipal());
        final RegistryUser user = componentRegistryFactory.getOrCreateUser(userCredentials);

        final List<String> groups = groupService.listGroupNames();
        for (String group : groups) {
            final Number groupId = groupService.getGroupIdByName(group);
            final ComponentRegistry registry = componentRegistryFactory.getComponentRegistry(RegistrySpace.GROUP, new OwnerUser(user.getId()), userCredentials, groupId);
            addRegistry(rootNode, registry, String.format("Registry of group %s", group));
        }
        TreeModel model = new DefaultTreeModel(rootNode);
        return model;
    }

    private void addRegistry(DefaultMutableTreeNode rootNode, ComponentRegistry registry, String name) throws UserUnauthorizedException, ItemNotFoundException, ComponentRegistryException {
        DefaultMutableTreeNode userNode = new DefaultMutableTreeNode(new DisplayDataNode(name, false));
        rootNode.add(userNode);
        add(userNode, registry);
    }

    private void add(DefaultMutableTreeNode parent, ComponentRegistry registry) throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException {
        DefaultMutableTreeNode componentsNode = new DefaultMutableTreeNode(new DisplayDataNode("Components", false));
        parent.add(componentsNode);
        add(componentsNode, registry.getComponentDescriptions(null), false, registry.getRegistrySpace());

        DefaultMutableTreeNode profilesNode = new DefaultMutableTreeNode(new DisplayDataNode("Profiles", false));
        parent.add(profilesNode);
        add(profilesNode, registry.getProfileDescriptions(null), false, registry.getRegistrySpace());

        DefaultMutableTreeNode deletedCompNode = new DefaultMutableTreeNode(new DisplayDataNode("Deleted Components", true));
        parent.add(deletedCompNode);

        List<ComponentDescription> deletedComponentDescriptions = registry.getDeletedComponentDescriptions();
        add(deletedCompNode, deletedComponentDescriptions, true, registry.getRegistrySpace());

        DefaultMutableTreeNode deletedProfNode = new DefaultMutableTreeNode(new DisplayDataNode("Deleted Profiles", true));
        parent.add(deletedProfNode);
        List<ProfileDescription> deletedProfileDescriptions = registry.getDeletedProfileDescriptions();
        add(deletedProfNode, deletedProfileDescriptions, true, registry.getRegistrySpace());
    }

    private void add(DefaultMutableTreeNode parent, List<? extends BaseDescription> descs, boolean isDeleted, RegistrySpace space) {
        for (BaseDescription desc : descs) {
            DefaultMutableTreeNode child = new DefaultMutableTreeNode(new DisplayDataNode(desc.getName(), isDeleted, desc, space));
            parent.add(child);
        }
    }

    @Override
    protected void addLinks() {
        //no call to super - no home link needed
    }

    private static class DisableOnDeletedBehavior extends Behavior {

        private final CMDItemInfo info;

        public DisableOnDeletedBehavior(CMDItemInfo info) {
            this.info = info;
        }

        @Override
        public void onConfigure(Component component) {
            component.setEnabled(info != null
                    && info.isEditable()
                    && info.getDataNode() != null
                    && !info.getDataNode().isDeleted());
        }

    }

    private class AdminTreeItemLeafNode extends Folder {

        private final IModel<DefaultMutableTreeNode> nodeModel;

        public AdminTreeItemLeafNode(String id, AbstractTree tree, IModel model, IModel<DefaultMutableTreeNode> nodeModel) {
            super(id, tree, model);
            this.nodeModel = nodeModel;
        }

        @Override
        protected boolean isClickable() {
            return true;
        }

        @Override
        protected boolean isSelected() {
            DisplayDataNode dn = (DisplayDataNode) nodeModel.getObject().getUserObject();
            if (info.getId() != null && dn.getDescription() != null) {
                return dn.getDescription().getId().equals(info.getId());
            }
            return super.isSelected();
        }

        @Override
        protected void onClick(AjaxRequestTarget target) {
            super.onClick(target);

            try {
                final DisplayDataNode dn = (DisplayDataNode) nodeModel.getObject().getUserObject();
                if (dn.getDescription() != null) {
                    //update description
                    dn.setDesc(ComponentUtils.toTypeByIdPrefix(componentDao.getDeletedById(dn.getDescription().getDbId())));
                }
                info.setDataNode(dn);
                
                final BaseDescription desc = dn.getDescription();
                if (desc != null) {
                    String content = componentDao.getContent(dn.isDeleted(), desc.getId());
                    info.setContent(content);
                }
            } catch (ComponentRegistryException ex) {
                LOG.error("Error getting node data", ex);
                getSession().error("Could not get data for node. See Tomcat log for details.");
            }
            if (target != null) {
                target.add(infoView);
                target.add(tree);
            }
        }
    }

}
