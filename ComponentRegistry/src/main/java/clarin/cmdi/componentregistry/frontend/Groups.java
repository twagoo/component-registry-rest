package clarin.cmdi.componentregistry.frontend;

import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.impl.database.GroupService;
import clarin.cmdi.componentregistry.impl.database.ValidationException;
import clarin.cmdi.componentregistry.model.RegistryUser;
import java.util.Collections;
import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class Groups extends SecureAdminWebPage {

    @SpringBean
    private GroupService groupService;
    private final IModel<Long> selectedGroup = new Model<Long>(null);

    public Groups(PageParameters parameters) {
        super(parameters);
        addLinks();

        add(createGroupForm("groupForm"));
        add(createGroupList("groups"));
        add(createGroupInfo("group"));
    }

    private Component createGroupForm(String id) {
        final IModel<String> nameModel = new Model<String>("");
        final IModel<String> ownerModel = new Model<String>("");

        final Form form = new Form(id) {

            @Override
            protected void onSubmit() {
                try {
                    final long groupId = groupService.createNewGroup(nameModel.getObject(), ownerModel.getObject());
                    info("Group with id " + groupId + " has been created");
                } catch (ValidationException ex) {
                    error(ex.getMessage());
                }
            }

        };
        form.add(new FeedbackPanel("feedback"));
        form.add(new TextField("name", nameModel).setRequired(true));
        form.add(new TextField("owner", ownerModel).setRequired(true));
        return form;
    }

    private Component createGroupList(String id) {
        final IModel<List> groupsModel = new AbstractReadOnlyModel<List>() {

            @Override
            public List getObject() {
                return groupService.listGroupNames();
            }
        };

        return new ListView(id, groupsModel) {

            @Override
            protected void populateItem(final ListItem li) {
                li.add(new Label("name", li.getModel()));
                li.add(new Link("select") {

                    @Override
                    public void onClick() {
                        final String groupName = (String) li.getModelObject();
                        try {
                            final Long groupId = (Long) groupService.getGroupIdByName(groupName);
                            selectedGroup.setObject(groupId);
                        } catch (ItemNotFoundException ex) {
                            selectedGroup.setObject(null);
                        }
                    }
                });
            }
        };

    }

    private Component createGroupInfo(String id) {
        final WebMarkupContainer container = new WebMarkupContainer(id) {

            @Override
            public boolean isVisible() {
                return selectedGroup.getObject() != null;
            }

        };

        container.add(new Label("name", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                try {
                    return groupService.getGroupNameById(selectedGroup.getObject());
                } catch (ItemNotFoundException ex) {
                    return "NOT FOUND";
                }
            }
        }));
        container.add(createNewMemberForm("newMember"));
        container.add(createGroupMembersView("members"));

        return container;
    }

    private Form createNewMemberForm(String id) {
        final IModel<String> principalModel = new Model<String>("");
        final Form memberForm = new Form(id) {

            @Override
            protected void onSubmit() {
                try {
                    final String groupName = groupService.getGroupNameById(selectedGroup.getObject());
                    groupService.makeMember(principalModel.getObject(), groupName);
                    info("User " + principalModel.getObject() + " added to group");
                } catch (ItemNotFoundException ex) {
                    error(ex);
                }
            }

        };
        memberForm.add(new FeedbackPanel("feedback"));
        memberForm.add(new TextField("principal", principalModel).setRequired(true));
        return memberForm;
    }

    private ListView createGroupMembersView(String id) {
        final IModel<List> membersModel = new AbstractReadOnlyModel<List>() {

            @Override
            public List getObject() {
                final Long groupId = selectedGroup.getObject();
                if (groupId == null) {
                    return Collections.emptyList();
                } else {
                    return groupService.getUsersInGroup(groupId);
                }
            }
        };
        final ListView membersView = new ListView(id, membersModel) {

            @Override
            protected void populateItem(ListItem li) {
                final RegistryUser user = (RegistryUser) li.getModelObject();
                li.add(new Label("name", user.getName()));
                li.add(new Link("remove") {

                    @Override
                    public void onClick() {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                });
            }
        };
        return membersView;
    }

}
