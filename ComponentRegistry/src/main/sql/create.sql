--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: comments; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE comments (
    id integer NOT NULL,
    profile_description_id integer,
    component_description_id integer,
    user_id integer,
    comments text NOT NULL
);


--
-- Name: component_description; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE component_description (
    id integer NOT NULL,
    user_id integer,
    content_id integer NOT NULL,
    is_public boolean NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    component_id character varying NOT NULL,
    name character varying NOT NULL,
    description character varying NOT NULL,
    registration_date timestamp with time zone,
    creator_name character varying,
    domain_name character varying
);


--
-- Name: component_description_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE component_description_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: component_description_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE component_description_id_seq OWNED BY component_description.id;


--
-- Name: cr_comments_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE cr_comments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: cr_comments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE cr_comments_id_seq OWNED BY comments.id;


--
-- Name: xml_content; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE xml_content (
    id integer NOT NULL,
    content xml
);


--
-- Name: cr_content_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE cr_content_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: cr_content_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE cr_content_id_seq OWNED BY xml_content.id;


--
-- Name: profile_description; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE profile_description (
    id integer NOT NULL,
    user_id integer,
    content_id integer NOT NULL,
    is_public boolean NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    profile_id character varying NOT NULL,
    name character varying NOT NULL,
    description character varying NOT NULL,
    registration_date timestamp with time zone,
    creator_name character varying,
    domain_name character varying
);


--
-- Name: profile_description_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE profile_description_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: profile_description_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE profile_description_id_seq OWNED BY profile_description.id;


--
-- Name: registry_user; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE registry_user (
    id integer NOT NULL,
    name character varying,
    principal_name character varying
);


--
-- Name: user_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


--
-- Name: user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE user_id_seq OWNED BY registry_user.id;


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE comments ALTER COLUMN id SET DEFAULT nextval('cr_comments_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE component_description ALTER COLUMN id SET DEFAULT nextval('component_description_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE profile_description ALTER COLUMN id SET DEFAULT nextval('profile_description_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE registry_user ALTER COLUMN id SET DEFAULT nextval('user_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE xml_content ALTER COLUMN id SET DEFAULT nextval('cr_content_id_seq'::regclass);


--
-- Name: component_description_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY component_description
    ADD CONSTRAINT component_description_pkey PRIMARY KEY (id);


--
-- Name: cr_comments_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY comments
    ADD CONSTRAINT cr_comments_pkey PRIMARY KEY (id);


--
-- Name: cr_xml_content_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY xml_content
    ADD CONSTRAINT cr_xml_content_pkey PRIMARY KEY (id);


--
-- Name: profile_description_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY profile_description
    ADD CONSTRAINT profile_description_pkey PRIMARY KEY (id);


--
-- Name: user_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY registry_user
    ADD CONSTRAINT user_pkey PRIMARY KEY (id);


--
-- Name: fki_comments_fk_user; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX fki_comments_fk_user ON comments USING btree (user_id);


--
-- Name: fki_comments_profile; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX fki_comments_profile ON comments USING btree (profile_description_id);


--
-- Name: fki_component_content; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX fki_component_content ON component_description USING btree (content_id);


--
-- Name: fki_component_user; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX fki_component_user ON component_description USING btree (user_id);


--
-- Name: fki_profile_content; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX fki_profile_content ON profile_description USING btree (content_id);


--
-- Name: fki_profile_user; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX fki_profile_user ON profile_description USING btree (user_id);


--
-- Name: comments_profile; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY comments
    ADD CONSTRAINT comments_profile FOREIGN KEY (profile_description_id) REFERENCES profile_description(id);


--
-- Name: comments_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY comments
    ADD CONSTRAINT comments_user FOREIGN KEY (user_id) REFERENCES registry_user(id);


--
-- Name: component_content; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY component_description
    ADD CONSTRAINT component_content FOREIGN KEY (content_id) REFERENCES xml_content(id);


--
-- Name: component_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY component_description
    ADD CONSTRAINT component_user FOREIGN KEY (user_id) REFERENCES registry_user(id);


--
-- Name: profile_content; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY profile_description
    ADD CONSTRAINT profile_content FOREIGN KEY (content_id) REFERENCES xml_content(id);


--
-- Name: profile_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY profile_description
    ADD CONSTRAINT profile_user FOREIGN KEY (user_id) REFERENCES registry_user(id);


--
-- Name: public; Type: ACL; Schema: -; Owner: -
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

