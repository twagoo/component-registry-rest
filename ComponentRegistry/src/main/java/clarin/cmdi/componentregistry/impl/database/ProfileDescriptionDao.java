package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.model.ProfileDescription;
import java.util.List;
import static clarin.cmdi.componentregistry.impl.database.ComponentDescriptionDatabase.*;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ProfileDescriptionDao extends AbstractDescriptionDao<ProfileDescription> {

    public ProfileDescriptionDao() {
        super(ProfileDescription.class);
    }

    @Override
    protected String getTableName() {
        return TABLE_PROFILE_DESCRIPTION;
    }

    @Override
    protected String getCMDIdColumn() {
        return "profile_id";
    }

    public List<ProfileDescription> getPublicProfileDescriptions() {
        return getPublicDescriptions();
    }
}
