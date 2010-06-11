package clarin.cmdi.componentregistry.frontend;

import java.io.File;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.markup.html.tree.ITreeState;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.model.PropertyModel;

import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.Configuration;
import clarin.cmdi.componentregistry.ResourceConfig;

public class AdminHomePage extends WebPage {

    private final FileInfo fileInfo = new FileInfo();

    @SuppressWarnings("serial")
    private class ItemEditForm extends Form<FileInfo> {
        public ItemEditForm(String name) {
            super(name);
            TextArea textArea = new TextArea("info", new PropertyModel<String>(fileInfo, "text"));
            textArea.setOutputMarkupId(true);
            add(textArea);
            add(new Button("submit") {
                @Override
                public void onSubmit() {
                      info("submitting:"+fileInfo.getName());

                }
            }.setDefaultFormProcessing(false));
            add(new Button("delete") {
                @Override
                public void onSubmit() {
                      info("deleting:"+fileInfo.getName());
//                      String dir = fileInfo.getFile().getParentFile().getName();
//                      if (ResourceConfig.DELETED_DIR_NAME.equals(dir)) {
//                          fileInfo.setText("Press undelete button to put this item back in the registry");
//                      } else if (ResourceConfig.COMPONENTS_DIR_NAME.equals(dir) || ResourceConfig.PROFILES_DIR_NAME.equals(dir)) {
//                          fileInfo.setText("Press delete button to delete this item");
//                      } else {
//                          fileInfo.setText("");
//                      }
                      //TODO PD load correct regisrty!! ComponentRegistryFactory.getInstance().getPublicRegistry().deleteMDComponent(componentId, principal);

                }
            }.setDefaultFormProcessing(false));
            add(new Button("undelete") {
                @Override
                public void onSubmit() {
                      info("undeleting:"+fileInfo.getName());

                }
            }.setDefaultFormProcessing(false));
        }
    }

    @SuppressWarnings("serial")
    public AdminHomePage(final PageParameters parameters) {
        add(new Label("message", "Component Registry Admin Page."));

        final FeedbackPanel feedback = new FeedbackPanel("feedback");
        add(feedback);
        Form form = new ItemEditForm("form");
        add(form);

        final LinkTree tree = createTree(form);
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
                if (fn.getFile().isFile()) {
                    fileInfo.setText(fn.getFileContent());
                } else {
                    String dir = fn.getFile().getParentFile().getName();
                    if (ResourceConfig.DELETED_DIR_NAME.equals(dir)) {
                        fileInfo.setText("Press undelete button to put this item back in the registry");
                    } else if (ResourceConfig.COMPONENTS_DIR_NAME.equals(dir) || ResourceConfig.PROFILES_DIR_NAME.equals(dir)) {
                        fileInfo.setText("Press delete button to delete this item");
                    } else {
                        fileInfo.setText("");
                    }
                }
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
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new FileNode(registryRoot));
        add(rootNode, registryRoot.listFiles());
        model = new DefaultTreeModel(rootNode);
        return model;
    }

    @SuppressWarnings("unchecked")
    private void add(DefaultMutableTreeNode parent, File[] files) {
        for (File file : files) {
            if (file.isDirectory()) {
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(new FileNode(file));
                parent.add(child);
                add(child, file.listFiles());
            } else {
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(new FileNode(file));
                parent.add(child);
            }
        }
    }

}
