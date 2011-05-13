package clarin.cmdi.componentregistry.impl.database;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public final class ComponentRegistryDatabase {

    private ComponentRegistryDatabase() {
    }

    public static void resetDatabase(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("DROP SCHEMA PUBLIC CASCADE");
    }

    public static void createTableComponentDescription(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE TABLE component_description ("
                + "id IDENTITY NOT NULL,"
                + "user_id integer,"
                + "    content_id integer NOT NULL,"
                + "  is_public boolean NOT NULL,"
                + "  is_deleted boolean DEFAULT false NOT NULL,"
                + "  component_id character varying NOT NULL,"
                + "  name character varying NOT NULL,"
                + "  description character varying NOT NULL,"
                + "  registration_date timestamp,"// with timezone,"
                + "  creator_name character varying,"
                + "  domain_name character varying);");
    }

    public static void createTableProfileDescription(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE TABLE profile_description ("
                + "id IDENTITY NOT NULL,"
                + "user_id integer,"
                + "    content_id integer NOT NULL,"
                + "  is_public boolean NOT NULL,"
                + "  is_deleted boolean DEFAULT false NOT NULL,"
                + "  profile_id character varying NOT NULL,"
                + "  name character varying NOT NULL,"
                + "  description character varying NOT NULL,"
                + "  registration_date timestamp,"// with timezone,"
                + "  creator_name character varying,"
                + "  domain_name character varying);");
    }

    public static void createTableXmlContent(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE TABLE xml_content ("
                + "id IDENTITY NOT NULL, content CHARACTER VARYING NOT NULL);");
    }
}
