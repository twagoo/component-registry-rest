package clarin.cmdi.componentregistry.impl.database;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public final class ComponentRegistryTestDatabase {

    private ComponentRegistryTestDatabase() {
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
                + "  href character varying,"
                + "  creator_name character varying,"
                + "  domain_name character varying,"
                + "  group_name character varying, CONSTRAINT UNIQUE_COMPONENT_ID UNIQUE (component_id));");
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
                + "  href character varying,"
                + "  creator_name character varying,"
                + "  domain_name character varying,"
                + "  group_name character varying, "
                + "  show_in_editor boolean DEFAULT true NOT NULL, "
                + "  CONSTRAINT UNIQUE_PROFILE_ID UNIQUE (profile_id));");
    }

    public static void createTableXmlContent(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE TABLE xml_content ("
                + "id IDENTITY NOT NULL, content CHARACTER VARYING NOT NULL);");
    }

    public static void createTableRegistryUser(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE TABLE registry_user ("
                + " id IDENTITY NOT NULL,"
                + " name character varying,"
                + " principal_name character varying);");
    }

    public static void createTableComments(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE TABLE comments ("
                + "id IDENTITY NOT NULL,"
                + "user_id integer,"
                + "  profile_description_id character varying,"
                + "  component_description_id character varying,"               
                + "  comments character varying NOT NULL,"
                + "  comment_date timestamp,"
                + "  CONSTRAINT UNIQUE_COMMENTS_ID UNIQUE (id));");
    }

    public static void resetAndCreateAllTables(JdbcTemplate jdbcTemplate) {
        resetDatabase(jdbcTemplate);
        createTableComponentDescription(jdbcTemplate);
        createTableProfileDescription(jdbcTemplate);
        createTableXmlContent(jdbcTemplate);
        createTableRegistryUser(jdbcTemplate);
        createTableComments(jdbcTemplate);
    }
}
