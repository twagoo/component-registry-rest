package clarin.cmdi.componentregistry.frontend;

import clarin.cmdi.componentregistry.impl.database.GroupService;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class Groups extends SecureAdminWebPage{

    @SpringBean
    private GroupService groupService;
    
    public Groups(PageParameters parameters) {
        super(parameters);
        addLinks();
    }
    
}
