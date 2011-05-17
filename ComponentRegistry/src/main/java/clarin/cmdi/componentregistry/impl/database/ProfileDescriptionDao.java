package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.model.ProfileDescription;
import java.util.List;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ProfileDescriptionDao extends AbstractDescriptionDao<ProfileDescription> {

    public ProfileDescriptionDao() {
        super(ProfileDescription.class);
    }

    public List<ProfileDescription> getPublicProfileDescriptions() {
        return getPublicDescriptions();
    }

    @Override
    protected String getTableName() {
        return TABLE_PROFILE_DESCRIPTION;
    }

    @Override
    protected String getCMDIdColumn() {
        return "profile_id";
    }
}
