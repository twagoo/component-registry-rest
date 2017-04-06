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
import clarin.cmdi.componentregistry.impl.database.AdminRegistry;
import clarin.cmdi.componentregistry.model.BaseDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import clarin.cmdi.componentregistry.model.RegistryUser;
import clarin.cmdi.componentregistry.persistence.ComponentDao;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.markup.html.tree.BaseTree;
import org.apache.wicket.extensions.markup.html.tree.ITreeState;
import org.apache.wicket.extensions.markup.html.tree.LinkTree;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class AdminHomePage extends SecureAdminWebPage {

    private static final long serialVersionUID = 1L;
    private final static Logger LOG = LoggerFactory.getLogger(AdminHomePage.class);
    private final CMDItemInfo info;
    private LinkTree tree;
    private transient AdminRegistry adminRegistry = new AdminRegistry();
    @SpringBean(name = "componentRegistryFactory")
    private ComponentRegistryFactory componentRegistryFactory;
    @SpringBean(name = "GroupService")
    private GroupService groupService;
    @SpringBean
    private ComponentDao componentDao;
    @SpringBean
    private IMarshaller marshaller;

    public AdminHomePage(final PageParameters parameters) throws ComponentRegistryException, ItemNotFoundException {
        super(parameters);
        adminRegistry.setComponentRegistryFactory(componentRegistryFactory);
        adminRegistry.setComponentDao(componentDao);
        adminRegistry.setMarshaller(marshaller);
        info = new CMDItemInfo(marshaller);
        addLinks();

        final FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        
        final Form form = createEditForm(feedback);
        add(new WebMarkupContainer("infoView")
                .add(feedback)
                .add(form)
                .setOutputMarkupId(true));

    }

    private Form createEditForm(final FeedbackPanel feedback) throws ItemNotFoundException, ComponentRegistryException {
        final Form form = new ItemEditForm("form");
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
                    target.add(form);
                    target.add(tree);
                    target.add(feedback);
                }
            }

            @Override
            public boolean isEnabled() {
                return info.isDeletable();

            }
            ;
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
                    target.add(form);
                    target.add(tree);
                    target.add(feedback);
                }
            }

            @Override
            public boolean isEnabled() {
                return info.isUndeletable();
            }
        };
        form.add(undeleteButton);
        CheckBox forceUpdateCheck = new CheckBox("forceUpdate");
        form.add(forceUpdateCheck);
        Button submitButton = new IndicatingAjaxButton("submit", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                CMDItemInfo info = (CMDItemInfo) form.getModelObject();
                Principal userPrincipal = getUserPrincipal();
                info("submitting:" + info.getName() + " id=(" + info.getDataNode().getDescription().getId() + ")");
                try {
                    adminRegistry.submitFile(info, userPrincipal);
                    info("submitting done.");
                } catch (Exception e) {
                    LOG.error("Admin: ", e);
                    error("Cannot submit: " + info.getName() + "\n error=" + e);
                }
                if (target != null) {
                    target.add(form);
                    target.add(feedback);
                }
            }

            @Override
            public boolean isEnabled() {
                return info.isEditable();
            }
        };
        form.add(submitButton);
        try {
            tree = createTree("tree", form, createDBTreeModel());
            add(tree);
            add(new Link("expandAll") {
                @Override
                public void onClick() {
                    tree.getTreeState().expandAll();
                }
            });

            add(new Link("collapseAll") {
                @Override
                public void onClick() {
                    tree.getTreeState().collapseAll();
                }
            });

        } catch (UserUnauthorizedException e) {
            LOG.error("Admin: ", e);
            error("Cannot create tree: error = " + e);
        }
        return form;
    }

    private void reloadTreeModel(CMDItemInfo info) throws UserUnauthorizedException, ItemNotFoundException {
        try {
            tree.setModelObject(createDBTreeModel());
        } catch (ComponentRegistryException e) {
            LOG.error("Admin: ", e);
            error("Cannot reload tree: " + info.getName() + "\n error=" + e);
        }
    }

    private LinkTree createTree(String id, final Form form, TreeModel treeModel) {
        final LinkTree adminTree = new LinkTree(id, treeModel) {
            @Override
            protected void onNodeLinkClicked(Object node, BaseTree tree, AjaxRequestTarget target) {
                super.onNodeLinkClicked(node, tree, target);
                ITreeState treeState = tree.getTreeState();
                if (treeState.isNodeExpanded(node)) {
                    treeState.collapseNode(node);
                } else {
                    treeState.expandNode(node);
                }
                DisplayDataNode dn = (DisplayDataNode) ((DefaultMutableTreeNode) node).getUserObject();
                info.setDataNode(dn);
                BaseDescription desc = dn.getDescription();
                if (desc != null) {
                    String content = componentDao.getContent(dn.isDeleted(), desc.getId());
                    info.setContent(content);
                }
                if (target != null) {
                    target.add(form);
                }
            }
        };
        return adminTree;
    }

    private class ItemEditForm extends Form<CMDItemInfo> {

        public ItemEditForm(String name) {
            super(name);
            CompoundPropertyModel model = new CompoundPropertyModel(info);
            setModel(model);

            TextArea descriptionArea = new TextArea("description");
            descriptionArea.setOutputMarkupId(true);
            add(descriptionArea);
            TextArea contentArea = new TextArea("content");
            contentArea.setOutputMarkupId(true);
            add(contentArea);
        }
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
}
