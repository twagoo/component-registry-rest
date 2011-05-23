package clarin.cmdi.componentregistry.frontend;

import clarin.cmdi.componentregistry.ComponentRegistryException;
import java.io.File;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.markup.html.tree.ITreeState;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.impl.filesystem.AdminRegistry;
import clarin.cmdi.componentregistry.impl.filesystem.FileSystemConfiguration;
import clarin.cmdi.componentregistry.impl.filesystem.ResourceConfig;
import clarin.cmdi.componentregistry.model.AbstractDescription;

@SuppressWarnings("serial")
public class AdminHomePage extends SecureAdminWebPage {
    private static final long serialVersionUID = 1L;
    private final static Logger LOG = LoggerFactory.getLogger(AdminHomePage.class);

    private final FileInfo fileInfo = new FileInfo();
    private final LinkTree tree;

    private transient AdminRegistry adminRegistry = new AdminRegistry();

    @SpringBean(name="componentRegistryFactory")
    private ComponentRegistryFactory componentRegistryFactory;

    public AdminHomePage(final PageParameters parameters) throws ComponentRegistryException {
        super(parameters);
        addLinks();
        final FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);
        Form form = new ItemEditForm("form");
        add(form);

        Button deleteButton = new AjaxFallbackButton("delete", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                FileInfo fileInfo = (FileInfo) form.getModelObject();
                info("deleting:" + fileInfo.getName());
                Principal userPrincipal = getWebRequest().getHttpServletRequest().getUserPrincipal();
                try {
                    adminRegistry.delete(fileInfo, userPrincipal);
                    tree.setModelObject(createTreeModel());
                    info("Item deleted.");
                } catch (SubmitFailedException e) {
                    LOG.error("Admin: ", e);
                    error("Cannot delete: " + fileInfo.getName() + "\n error=" + e);
                }
                if (target != null) {
                    target.addComponent(form);
                    target.addComponent(tree);
                    target.addComponent(feedback);
                }
            }

            public boolean isEnabled() {
                return fileInfo.isDeletable();
            };
        };
        form.add(deleteButton);

        Button undeleteButton = new AjaxFallbackButton("undelete", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                FileInfo fileInfo = (FileInfo) form.getModelObject();
                info("undeleting:" + fileInfo.getName());
                Principal userPrincipal = getWebRequest().getHttpServletRequest().getUserPrincipal();
                try {
                    adminRegistry.undelete(fileInfo, userPrincipal);
                    info("Item put back.");
                    tree.setModelObject(createTreeModel());
                } catch (SubmitFailedException e) {
                    LOG.error("Admin: ", e);
                    error("Cannot undelete: " + fileInfo.getName() + "\n error=" + e);
                }
                if (target != null) {
                    target.addComponent(form);
                    target.addComponent(tree);
                    target.addComponent(feedback);
                }
            }

            public boolean isEnabled() {
                return fileInfo.isUndeletable();
            }

        };
        form.add(undeleteButton);

        CheckBox forceUpdateCheck = new CheckBox("forceUpdate");
        form.add(forceUpdateCheck);

        Button submitButton = new AjaxFallbackButton("submit", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                FileInfo fileInfo = (FileInfo) form.getModelObject();
                Principal userPrincipal = getWebRequest().getHttpServletRequest().getUserPrincipal();
                info("submitting:" + fileInfo.getName() + " id=(" + fileInfo.getDisplayNode().getId() + ")");
                try {
                    adminRegistry.submitFile(fileInfo, userPrincipal);
                    info("submitting done.");
                } catch (SubmitFailedException e) {
                    LOG.error("Admin: ", e);
                    error("Cannot submit: " + fileInfo.getName() + "\n error=" + e);
                }
                if (target != null) {
                    target.addComponent(form);
                    target.addComponent(feedback);
                }
            }

            public boolean isEnabled() {
                return fileInfo.isEditable();
            }

        };
        form.add(submitButton);

        tree = createTree("tree", form, createTreeModel());
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

        LinkTree dbTree = createTree("tree2", form, createDBTreeModel());
        add(dbTree);
    }

    private void addLinks() {
        add(new Label("linksMessage", "Browse the data below or choose on of the following options:"));
        add(new Link("massMigrate") {
            @Override
            public void onClick() {
                setResponsePage(MassMigratePage.class);
            }
        });
        add(new Link("log") {
            @Override
            public void onClick() {
                setResponsePage(ViewLogPage.class);
            }
        });
        add(new Link("statistics") {
            @Override
            public void onClick() {
                setResponsePage(StatisticsPage.class);
            }
        });
    }

    private LinkTree createTree(String id, final Form form, TreeModel treeModel) {
        final LinkTree tree = new LinkTree(id, treeModel) {
            @Override
            protected void onNodeLinkClicked(Object node, BaseTree tree, AjaxRequestTarget target) {
                super.onNodeLinkClicked(node, tree, target);
                ITreeState treeState = tree.getTreeState();
                if (treeState.isNodeExpanded(node)) {
                    treeState.collapseNode(node);
                } else {
                    treeState.expandNode(node);
                }
                DisplayNode dn = (DisplayNode) ((DefaultMutableTreeNode) node).getUserObject();
                fileInfo.setDisplayNode(dn);
                if (target != null) {
                    target.addComponent(form);
                }
            }
        };
        return tree;
    }

    private TreeModel createTreeModel() {
        File registryRoot = FileSystemConfiguration.getInstance().getRegistryRoot();
        TreeModel model = null;
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new FileNode(registryRoot, false));
        add(rootNode, registryRoot.listFiles(), false);
        model = new DefaultTreeModel(rootNode);
        return model;
    }

    @SuppressWarnings("unchecked")
    private void add(DefaultMutableTreeNode parent, File[] files, boolean isDeleted) {
        for (File file : files) {
            if (file.isDirectory()) {
                boolean deleted = ResourceConfig.DELETED_DIR_NAME.equals(file.getName()) || isDeleted; //once you find a deleted dir mark all child nodes in the tree as deleted.
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(new FileNode(file, deleted));
                parent.add(child);
                add(child, file.listFiles(), deleted);
            } else {
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(new FileNode(file, isDeleted));
                parent.add(child);
            }
        }
    }

    private class ItemEditForm extends Form<FileInfo> {

        public ItemEditForm(String name) {
            super(name);
            CompoundPropertyModel model = new CompoundPropertyModel(fileInfo);
            setModel(model);

            TextArea textArea = new TextArea("text");
            textArea.setOutputMarkupId(true);
            add(textArea);
        }

    }

    private TreeModel createDBTreeModel() throws ComponentRegistryException {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new DisplayDataNode("ComponentRegistry", false));
        DefaultMutableTreeNode publicNode = new DefaultMutableTreeNode(new DisplayDataNode("Public", false));
        rootNode.add(publicNode);
        ComponentRegistry publicRegistry = componentRegistryFactory.getPublicRegistry();
        add(publicNode, publicRegistry);
        List<ComponentRegistry> userRegistries = componentRegistryFactory.getAllUserRegistries();
        int i = 0;
        for (ComponentRegistry registry : userRegistries) {
            DefaultMutableTreeNode userNode = new DefaultMutableTreeNode(new DisplayDataNode("user" + i++, false)); //TODO PD should have a registry.getName()
            rootNode.add(userNode);
            add(userNode, registry);
        }
        TreeModel model = new DefaultTreeModel(rootNode);
        return model;
    }

    private void add(DefaultMutableTreeNode parent, ComponentRegistry registry) throws ComponentRegistryException {
        DefaultMutableTreeNode componentsNode = new DefaultMutableTreeNode(new DisplayDataNode("Components", false));
        parent.add(componentsNode);
        add(componentsNode, registry.getComponentDescriptions(), false);

        DefaultMutableTreeNode profilesNode = new DefaultMutableTreeNode(new DisplayDataNode("Profiles", false));
        parent.add(profilesNode);
        add(profilesNode, registry.getProfileDescriptions(), false);

        DefaultMutableTreeNode deletedCompNode = new DefaultMutableTreeNode(new DisplayDataNode("Deleted Components", true));
        parent.add(deletedCompNode);
        add(deletedCompNode, new ArrayList(), true); //TODO PD implement getDeletedComps/profiles

        DefaultMutableTreeNode deletedProfNode = new DefaultMutableTreeNode(new DisplayDataNode("Deleted Profiles", true));
        parent.add(deletedProfNode);
        add(deletedProfNode, new ArrayList(), true);
    }

    private void add(DefaultMutableTreeNode parent, List<? extends AbstractDescription> descs, boolean isDeleted) {
        for (AbstractDescription desc : descs) {
            DefaultMutableTreeNode child = new DefaultMutableTreeNode(new DisplayDataNode(desc.getName(), isDeleted));
            parent.add(child);
        }
    }

}
