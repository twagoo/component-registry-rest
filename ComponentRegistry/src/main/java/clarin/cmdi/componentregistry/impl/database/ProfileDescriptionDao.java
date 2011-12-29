package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ProfileDescription;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.ListUtils;

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

    @Override
    protected String getCommentsForeignKeyColumn() {
	return "profile_description_id";
    }

    @Override
    protected StringBuilder getDescriptionColumnList() {
	return super.getDescriptionColumnList().append(",show_in_editor");
    }

    @Override
    protected void putInsertParameters(Map<String, Object> params, AbstractDescription description, Number contentId, Number userId, boolean isPublic) {
	super.putInsertParameters(params, description, contentId, userId, isPublic);
	params.put("show_in_editor", ((ProfileDescription) description).isShowInEditor());
    }

    @Override
    protected void setDescriptionValuesFromResultSet(ResultSet rs, AbstractDescription newDescription) throws SQLException {
	super.setDescriptionValuesFromResultSet(rs, newDescription);
	((ProfileDescription) newDescription).setShowInEditor(rs.getBoolean("show_in_editor"));
    }

    @Override
    protected void appendUpdateColumnsStatement(StringBuilder updateDescription) {
	super.appendUpdateColumnsStatement(updateDescription);
	updateDescription.append(" , show_in_editor=?");
    }

    @Override
    protected List getUpdateParameterValues(AbstractDescription description) {
	return ListUtils.union(
		super.getUpdateParameterValues(description),
		// Add value for 'shown_in_editor' to abstract description
		Collections.singletonList(((ProfileDescription) description).isShowInEditor()));
    }
}
