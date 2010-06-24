package clarin.cmdi.componentregistry.frontend;

import java.io.File;
import java.security.Principal;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.markup.html.tree.ITreeState;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.model.CompoundPropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import clarin.cmdi.componentregistry.AdminRegistry;
import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.ResourceConfig;

public class AdminHomePage extends WebPage {
    private final static Logger LOG = LoggerFactory.getLogger(AdminHomePage.class);

    private final FileInfo fileInfo = new FileInfo();
    private final LinkTree tree;

    private transient AdminRegistry adminRegistry = new AdminRegistry();

    @SuppressWarnings("serial")
    public AdminHomePage(final PageParameters parameters) {
        Principal userPrincipal = getWebRequestCycle().getWebRequest().getHttpServletRequest().getUserPrincipal();
        if (!Configuration.getInstance().isAdminUser(userPrincipal)) {
            setResponsePage(new AccessDeniedPage());
        }
        add(new MultiLineLabel("message", "Component Registry Admin Page.\nYou are logged in as: " + userPrincipal.getName()
                + ".\nRegistry is located in: " + Configuration.getInstance().getRegistryRoot()));
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
                    error("Cannot undelete: " + fileInfo.getName() + "\n error=" + e);
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

        Button submitButton = new AjaxFallbackButton("submit", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                FileInfo fileInfo = (FileInfo) form.getModelObject();
                Principal userPrincipal = getWebRequest().getHttpServletRequest().getUserPrincipal();
                info("submitting:" + fileInfo.getName() + " id=(" + fileInfo.getFileNode().getFile().getParentFile().getName() + ")");
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

        tree = createTree(form);
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

    }

    @SuppressWarnings("serial")
    private LinkTree createTree(final Form form) {
        TreeModel treeModel = createTreeModel();
        final LinkTree tree = new LinkTree("tree", treeModel) {
            @Override
            protected void onNodeLinkClicked(Object node, BaseTree tree, AjaxRequestTarget target) {
                super.onNodeLinkClicked(node, tree, target);
                ITreeState treeState = tree.getTreeState();
                if (treeState.isNodeExpanded(node)) {
                    treeState.collapseNode(node);
                } else {
                    treeState.expandNode(node);
                }
                FileNode fn = (FileNode) ((DefaultMutableTreeNode) node).getUserObject();
                fileInfo.setFileNode(fn);
                if (target != null) {
                    target.addComponent(form);
                }
            }
        };
        return tree;
    }

    private TreeModel createTreeModel() {
        File registryRoot = Configuration.getInstance().getRegistryRoot();
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

    @SuppressWarnings("serial")
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
}
