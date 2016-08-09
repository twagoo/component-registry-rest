--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

--
-- Name: item_status; Type: TYPE; Schema: public; Owner: compreg
--

CREATE TYPE item_status AS ENUM (
    'development',
    'production',
    'deprecated'
);


ALTER TYPE item_status OWNER TO compreg;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: basedescription; Type: TABLE; Schema: public; Owner: compreg; Tablespace: 
--

CREATE TABLE basedescription (
    id integer NOT NULL,
    user_id integer,
    is_public boolean NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    component_id character varying NOT NULL,
    name character varying NOT NULL,
    description character varying NOT NULL,
    registration_date timestamp with time zone,
    creator_name character varying,
    domain_name character varying,
    group_name character varying,
    href character varying,
    show_in_editor boolean DEFAULT true NOT NULL,
    content text DEFAULT ''::text NOT NULL,
    status item_status,
    derivedfrom integer,
    successor integer
);


ALTER TABLE basedescription OWNER TO compreg;

--
-- Name: basedescription_id_seq; Type: SEQUENCE; Schema: public; Owner: compreg
--

CREATE SEQUENCE basedescription_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE basedescription_id_seq OWNER TO compreg;

--
-- Name: basedescription_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: compreg
--

ALTER SEQUENCE basedescription_id_seq OWNED BY basedescription.id;


--
-- Name: comments; Type: TABLE; Schema: public; Owner: compreg; Tablespace: 
--

CREATE TABLE comments (
    id integer NOT NULL,
    comments text NOT NULL,
    comment_date timestamp with time zone NOT NULL,
    component_id character varying,
    user_id integer NOT NULL,
    user_name character varying
);


ALTER TABLE comments OWNER TO compreg;

--
-- Name: comments_id_seq; Type: SEQUENCE; Schema: public; Owner: compreg
--

CREATE SEQUENCE comments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE comments_id_seq OWNER TO compreg;

--
-- Name: comments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: compreg
--

ALTER SEQUENCE comments_id_seq OWNED BY comments.id;


--
-- Name: groupmembership; Type: TABLE; Schema: public; Owner: compreg; Tablespace: 
--

CREATE TABLE groupmembership (
    id integer NOT NULL,
    groupid integer,
    userid integer
);


ALTER TABLE groupmembership OWNER TO compreg;

--
-- Name: groupmembership_id_seq; Type: SEQUENCE; Schema: public; Owner: compreg
--

CREATE SEQUENCE groupmembership_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE groupmembership_id_seq OWNER TO compreg;

--
-- Name: groupmembership_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: compreg
--

ALTER SEQUENCE groupmembership_id_seq OWNED BY groupmembership.id;


--
-- Name: ownership; Type: TABLE; Schema: public; Owner: compreg; Tablespace: 
--

CREATE TABLE ownership (
    id integer NOT NULL,
    componentid character varying(255),
    groupid integer,
    userid integer
);


ALTER TABLE ownership OWNER TO compreg;

--
-- Name: ownership_id_seq; Type: SEQUENCE; Schema: public; Owner: compreg
--

CREATE SEQUENCE ownership_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE ownership_id_seq OWNER TO compreg;

--
-- Name: ownership_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: compreg
--

ALTER SEQUENCE ownership_id_seq OWNED BY ownership.id;


--
-- Name: registry_user; Type: TABLE; Schema: public; Owner: compreg; Tablespace: 
--

CREATE TABLE registry_user (
    id integer NOT NULL,
    name character varying,
    principal_name character varying
);


ALTER TABLE registry_user OWNER TO compreg;

--
-- Name: registry_user_id_seq; Type: SEQUENCE; Schema: public; Owner: compreg
--

CREATE SEQUENCE registry_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE registry_user_id_seq OWNER TO compreg;

--
-- Name: registry_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: compreg
--

ALTER SEQUENCE registry_user_id_seq OWNED BY registry_user.id;


--
-- Name: usergroup; Type: TABLE; Schema: public; Owner: compreg; Tablespace: 
--

CREATE TABLE usergroup (
    id integer NOT NULL,
    ownerid integer NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE usergroup OWNER TO compreg;

--
-- Name: usergroup_id_seq; Type: SEQUENCE; Schema: public; Owner: compreg
--

CREATE SEQUENCE usergroup_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE usergroup_id_seq OWNER TO compreg;

--
-- Name: usergroup_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: compreg
--

ALTER SEQUENCE usergroup_id_seq OWNED BY usergroup.id;


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: compreg
--

ALTER TABLE ONLY basedescription ALTER COLUMN id SET DEFAULT nextval('basedescription_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: compreg
--

ALTER TABLE ONLY comments ALTER COLUMN id SET DEFAULT nextval('comments_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: compreg
--

ALTER TABLE ONLY groupmembership ALTER COLUMN id SET DEFAULT nextval('groupmembership_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: compreg
--

ALTER TABLE ONLY ownership ALTER COLUMN id SET DEFAULT nextval('ownership_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: compreg
--

ALTER TABLE ONLY registry_user ALTER COLUMN id SET DEFAULT nextval('registry_user_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: compreg
--

ALTER TABLE ONLY usergroup ALTER COLUMN id SET DEFAULT nextval('usergroup_id_seq'::regclass);


--
-- Name: basedescription_pkey; Type: CONSTRAINT; Schema: public; Owner: compreg; Tablespace: 
--

ALTER TABLE ONLY basedescription
    ADD CONSTRAINT basedescription_pkey PRIMARY KEY (id);


--
-- Name: comments_id_pkey; Type: CONSTRAINT; Schema: public; Owner: compreg; Tablespace: 
--

ALTER TABLE ONLY comments
    ADD CONSTRAINT comments_id_pkey PRIMARY KEY (id);


--
-- Name: constraint_basedescription_unique_id; Type: CONSTRAINT; Schema: public; Owner: compreg; Tablespace: 
--

ALTER TABLE ONLY basedescription
    ADD CONSTRAINT constraint_basedescription_unique_id UNIQUE (id);


--
-- Name: groupmembership_pkey; Type: CONSTRAINT; Schema: public; Owner: compreg; Tablespace: 
--

ALTER TABLE ONLY groupmembership
    ADD CONSTRAINT groupmembership_pkey PRIMARY KEY (id);


--
-- Name: ownership_pkey; Type: CONSTRAINT; Schema: public; Owner: compreg; Tablespace: 
--

ALTER TABLE ONLY ownership
    ADD CONSTRAINT ownership_pkey PRIMARY KEY (id);


--
-- Name: registry_user_principal_name_key; Type: CONSTRAINT; Schema: public; Owner: compreg; Tablespace: 
--

ALTER TABLE ONLY registry_user
    ADD CONSTRAINT registry_user_principal_name_key UNIQUE (principal_name);


--
-- Name: user_pkey; Type: CONSTRAINT; Schema: public; Owner: compreg; Tablespace: 
--

ALTER TABLE ONLY registry_user
    ADD CONSTRAINT user_pkey PRIMARY KEY (id);


--
-- Name: usergroup_pkey; Type: CONSTRAINT; Schema: public; Owner: compreg; Tablespace: 
--

ALTER TABLE ONLY usergroup
    ADD CONSTRAINT usergroup_pkey PRIMARY KEY (id);


--
-- Name: fki_comments_component_id; Type: INDEX; Schema: public; Owner: compreg; Tablespace: 
--

CREATE INDEX fki_comments_component_id ON comments USING btree (component_id);


--
-- Name: fki_comments_fk_user; Type: INDEX; Schema: public; Owner: compreg; Tablespace: 
--

CREATE INDEX fki_comments_fk_user ON comments USING btree (user_id);


--
-- Name: idx_basedescription_id; Type: INDEX; Schema: public; Owner: compreg; Tablespace: 
--

CREATE INDEX idx_basedescription_id ON basedescription USING btree (id);


--
-- Name: idx_component_id; Type: INDEX; Schema: public; Owner: compreg; Tablespace: 
--

CREATE INDEX idx_component_id ON basedescription USING btree (component_id);


--
-- Name: idx_is_deleted; Type: INDEX; Schema: public; Owner: compreg; Tablespace: 
--

CREATE INDEX idx_is_deleted ON basedescription USING btree (is_deleted);


--
-- Name: idx_is_public; Type: INDEX; Schema: public; Owner: compreg; Tablespace: 
--

CREATE INDEX idx_is_public ON basedescription USING btree (is_public);


--
-- Name: idx_user_id; Type: INDEX; Schema: public; Owner: compreg; Tablespace: 
--

CREATE INDEX idx_user_id ON basedescription USING btree (user_id);


--
-- Name: comments_user; Type: FK CONSTRAINT; Schema: public; Owner: compreg
--

ALTER TABLE ONLY comments
    ADD CONSTRAINT comments_user FOREIGN KEY (user_id) REFERENCES registry_user(id);


--
-- Name: fk_basedescription_user_id; Type: FK CONSTRAINT; Schema: public; Owner: compreg
--

ALTER TABLE ONLY basedescription
    ADD CONSTRAINT fk_basedescription_user_id FOREIGN KEY (user_id) REFERENCES registry_user(id) MATCH FULL;


--
-- PostgreSQL database dump complete
--

grant all on basedescription, basedescription_id_seq, comments, comments_id_seq, groupmembership, groupmembership_id_seq, ownership, ownership_id_seq, registry_user, registry_user_id_seq, usergroup, usergroup_id_seq to compreg;

