package clarin.cmdi.componentregistry.frontend;

import java.security.Principal;
import java.util.Collections;
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
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.ComponentRegistryFactory;
import clarin.cmdi.componentregistry.impl.database.ComponentDescriptionDao;
import clarin.cmdi.componentregistry.impl.database.ProfileDescriptionDao;
import clarin.cmdi.componentregistry.impl.filesystem.AdminRegistry;
import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;

@SuppressWarnings("serial")
public class AdminHomePage extends SecureAdminWebPage {
    private static final long serialVersionUID = 1L;
    private final static Logger LOG = LoggerFactory.getLogger(AdminHomePage.class);

    private final CMDItemInfo info = new CMDItemInfo();
    private final LinkTree tree;

    private transient AdminRegistry adminRegistry = new AdminRegistry();

    @SpringBean(name = "componentRegistryFactory")
    private ComponentRegistryFactory componentRegistryFactory;
    @SpringBean
    private ProfileDescriptionDao profileDescriptionDao;
    @SpringBean
    private ComponentDescriptionDao componentDescriptionDao;

    public AdminHomePage(final PageParameters parameters) throws ComponentRegistryException {
	super(parameters);
	adminRegistry.setComponentRegistryFactory(componentRegistryFactory);
	adminRegistry.setProfileDescriptionDao(profileDescriptionDao);
	adminRegistry.setComponentDescriptionDao(componentDescriptionDao);
	addLinks();
	final FeedbackPanel feedback = new FeedbackPanel("feedback");
	feedback.setOutputMarkupId(true);
	add(feedback);
	Form form = new ItemEditForm("form");
	add(form);

	Button deleteButton = new AjaxFallbackButton("delete", form) {
	    @Override
	    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
		CMDItemInfo info = (CMDItemInfo) form.getModelObject();
		info("deleting:" + info.getName());
		Principal userPrincipal = getWebRequest().getHttpServletRequest().getUserPrincipal();
		try {
		    adminRegistry.delete(info, userPrincipal);
		    info("Item deleted.");
		    reloadTreeModel(info);
		} catch (SubmitFailedException e) {
		    LOG.error("Admin: ", e);
		    error("Cannot delete: " + info.getName() + "\n error=" + e);
		}
		if (target != null) {
		    target.addComponent(form);
		    target.addComponent(tree);
		    target.addComponent(feedback);
		}
	    }

	    public boolean isEnabled() {
		return info.isDeletable();

	    };
	};
	form.add(deleteButton);

	Button undeleteButton = new AjaxFallbackButton("undelete", form) {
	    @Override
	    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
		CMDItemInfo info = (CMDItemInfo) form.getModelObject();
		info("undeleting:" + info.getName());
		try {
		    adminRegistry.undelete(info);
		    info("Item put back.");
		    reloadTreeModel(info);
		} catch (SubmitFailedException e) {
		    LOG.error("Admin: ", e);
		    error("Cannot undelete: " + info.getName() + "\n error=" + e);
		}
		if (target != null) {
		    target.addComponent(form);
		    target.addComponent(tree);
		    target.addComponent(feedback);
		}
	    }

	    public boolean isEnabled() {
		return info.isUndeletable();
	    }

	};
	form.add(undeleteButton);

	CheckBox forceUpdateCheck = new CheckBox("forceUpdate");
	form.add(forceUpdateCheck);

	Button submitButton = new AjaxFallbackButton("submit", form) {
	    @Override
	    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
		CMDItemInfo info = (CMDItemInfo) form.getModelObject();
		Principal userPrincipal = getWebRequest().getHttpServletRequest().getUserPrincipal();
		info("submitting:" + info.getName() + " id=(" + info.getDataNode().getDescription().getId() + ")");
		try {
		    adminRegistry.submitFile(info, userPrincipal);
		    info("submitting done.");
		    reloadTreeModel(info);
		} catch (SubmitFailedException e) {
		    LOG.error("Admin: ", e);
		    error("Cannot submit: " + info.getName() + "\n error=" + e);
		}
		if (target != null) {
		    target.addComponent(form);
		    target.addComponent(tree);
		    target.addComponent(feedback);
		}
	    }

	    public boolean isEnabled() {
		return info.isEditable();
	    }

	};
	form.add(submitButton);

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

    }

    private void reloadTreeModel(CMDItemInfo info) {
	try {
	    tree.setModelObject(createDBTreeModel());
	} catch (ComponentRegistryException e) {
	    LOG.error("Admin: ", e);
	    error("Cannot reload tree: " + info.getName() + "\n error=" + e);
	}
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
		DisplayDataNode dn = (DisplayDataNode) ((DefaultMutableTreeNode) node).getUserObject();
		info.setDataNode(dn);
		AbstractDescription desc = dn.getDescription();
		if (desc != null) {
		    String content;
		    if (desc.isProfile()) {
			content = profileDescriptionDao.getContent(dn.isDeleted(), desc.getId());
		    } else {
			content = componentDescriptionDao.getContent(dn.isDeleted(), desc.getId());
		    }
		    info.setContent(content);
		}
		if (target != null) {
		    target.addComponent(form);
		}
	    }
	};
	return tree;
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

    private TreeModel createDBTreeModel() throws ComponentRegistryException {
	DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new DisplayDataNode("ComponentRegistry", false));
	DefaultMutableTreeNode publicNode = new DefaultMutableTreeNode(new DisplayDataNode("Public", false));
	rootNode.add(publicNode);
	ComponentRegistry publicRegistry = componentRegistryFactory.getPublicRegistry();
	add(publicNode, publicRegistry);
	List<ComponentRegistry> userRegistries = componentRegistryFactory.getAllUserRegistries();
	for (ComponentRegistry registry : userRegistries) {
	    DefaultMutableTreeNode userNode = new DefaultMutableTreeNode(new DisplayDataNode(registry.getName(), false));
	    rootNode.add(userNode);
	    add(userNode, registry);
	}
	TreeModel model = new DefaultTreeModel(rootNode);
	return model;
    }

    private void add(DefaultMutableTreeNode parent, ComponentRegistry registry) throws ComponentRegistryException {
	DefaultMutableTreeNode componentsNode = new DefaultMutableTreeNode(new DisplayDataNode("Components", false));
	parent.add(componentsNode);
	add(componentsNode, registry.getComponentDescriptions(), false, registry.isPublic());

	DefaultMutableTreeNode profilesNode = new DefaultMutableTreeNode(new DisplayDataNode("Profiles", false));
	parent.add(profilesNode);
	add(profilesNode, registry.getProfileDescriptions(), false, registry.isPublic());

	DefaultMutableTreeNode deletedCompNode = new DefaultMutableTreeNode(new DisplayDataNode("Deleted Components", true));
	parent.add(deletedCompNode);

	List<ComponentDescription> deletedComponentDescriptions = registry.getDeletedComponentDescriptions();
	add(deletedCompNode, deletedComponentDescriptions, true, registry.isPublic());

	DefaultMutableTreeNode deletedProfNode = new DefaultMutableTreeNode(new DisplayDataNode("Deleted Profiles", true));
	parent.add(deletedProfNode);
	List<ProfileDescription> deletedProfileDescriptions = registry.getDeletedProfileDescriptions();
	add(deletedProfNode, deletedProfileDescriptions, true, registry.isPublic());
    }

    private void add(DefaultMutableTreeNode parent, List<? extends AbstractDescription> descs, boolean isDeleted, boolean isPublic) {
	Collections.sort(descs, AbstractDescription.COMPARE_ON_NAME);
	for (AbstractDescription desc : descs) {
	    DefaultMutableTreeNode child = new DefaultMutableTreeNode(new DisplayDataNode(desc.getName(), isDeleted, desc, isPublic));
	    parent.add(child);
	}
    }

}
