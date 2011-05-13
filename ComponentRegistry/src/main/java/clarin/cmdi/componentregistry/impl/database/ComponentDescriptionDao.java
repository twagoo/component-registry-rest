package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.model.ComponentDescription;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ComponentDescriptionDao extends SimpleJdbcDaoSupport {

    public List<ComponentDescription> getPublicComponentDescriptions() {
        String select = "select name, description from component_description";

        ParameterizedRowMapper<ComponentDescription> rowMapper = new ParameterizedRowMapper<ComponentDescription>() {

            @Override
            public ComponentDescription mapRow(ResultSet rs, int rowNumber) throws SQLException {
                ComponentDescription cd = new ComponentDescription();
                cd.setName(rs.getString("name"));
                cd.setDescription(rs.getString("description"));
                return cd;
            }
        };

        return getSimpleJdbcTemplate().query(select, rowMapper);
    }

    public void insertComponent(final AbstractDescription description, final String content) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(getDataSource())
                .withTableName("xml_content")
                .usingGeneratedKeyColumns("id");
        Number newId = insert.executeAndReturnKey(Collections.singletonMap("content", (Object)content));

        SimpleJdbcInsert insertDescription = new SimpleJdbcInsert(getDataSource())
                .withTableName("component_description")
                .usingGeneratedKeyColumns("id");
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("content_id", newId);
        params.put("is_public", Boolean.TRUE);
        params.put("is_deleted", Boolean.FALSE);
        params.put("component_id", "123");
        params.put("name", description.getName());
        params.put("description", description.getDescription());
        insertDescription.execute(params);
    }
}
