package clarin.cmdi.componentregistry.frontend;

import clarin.cmdi.componentregistry.impl.database.GroupService;
import clarin.cmdi.componentregistry.impl.database.ValidationException;
import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.IBehavior;
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
    private final IModel<String> selectedGroup = new Model<String>(null);

    public Groups(PageParameters parameters) {
        super(parameters);
        addLinks();

        add(createGroupForm("groupForm"));
        add(createGroupList("groups"));
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
                        selectedGroup.setObject((String) li.getModelObject());
                    }
                });
            }
        };

    }

}
