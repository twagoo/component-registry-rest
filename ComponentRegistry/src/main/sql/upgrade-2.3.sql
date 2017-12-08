-- Table: public.itemlock

-- DROP TABLE public.itemlock;

CREATE TABLE itemlock
(
    id integer NOT NULL DEFAULT nextval('itemlock_id_seq'::regclass),
    itemid integer NOT NULL,
    userid integer NOT NULL,
    creationdate timestamp with time zone NOT NULL DEFAULT now(),
    CONSTRAINT itemlock_pkey PRIMARY KEY (id),
    CONSTRAINT lock UNIQUE (itemid),
    CONSTRAINT itemid FOREIGN KEY (itemid)
        REFERENCES basedescription (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT userid FOREIGN KEY (userid)
        REFERENCES registry_user (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE itemlock
    OWNER to compreg;
    