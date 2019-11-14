-- Table: public.itemlock

-- DROP TABLE public.itemlock;
    
--
-- Name: itemlock_id_seq; Type: SEQUENCE; Schema: public; Owner: compreg
--

CREATE SEQUENCE IF NOT EXISTS itemlock_id_seq
    START WITH 2
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE itemlock_id_seq OWNER TO compreg;

--
-- Name: itemlock; Type: TABLE; Schema: public; Owner: compreg
--

CREATE TABLE IF NOT EXISTS itemlock (
    id integer DEFAULT nextval('itemlock_id_seq'::regclass) NOT NULL,
    itemid integer NOT NULL,
    userid integer NOT NULL,
    creationdate timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE itemlock OWNER TO compreg;

--
-- Name: itemlock itemlock_pkey; Type: CONSTRAINT; Schema: public; Owner: compreg
--

ALTER TABLE ONLY itemlock
    ADD CONSTRAINT itemlock_pkey PRIMARY KEY (id);


--
-- Name: itemlock lock; Type: CONSTRAINT; Schema: public; Owner: compreg
--

ALTER TABLE ONLY itemlock
    ADD CONSTRAINT lock UNIQUE (itemid);

--
-- Name: itemlock item_id; Type: FK CONSTRAINT; Schema: public; Owner: compreg
--

ALTER TABLE ONLY itemlock
    ADD CONSTRAINT itemid FOREIGN KEY (itemid) REFERENCES basedescription(id);


--
-- Name: itemlock user_id; Type: FK CONSTRAINT; Schema: public; Owner: compreg
--

ALTER TABLE ONLY itemlock
    ADD CONSTRAINT userid FOREIGN KEY (userid) REFERENCES registry_user(id);
