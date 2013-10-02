CREATE TABLE usergroup
(
  id serial,
  ownerId integer NOT NULL,
  name varchar(255) NOT NULL,
  CONSTRAINT usergroup_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
GRANT ALL ON TABLE usergroup TO postgres;
GRANT ALL ON TABLE usergroup TO compreg;
grant all on usergroup_id_seq to compreg;

CREATE TABLE ownership
(
  id serial,
  profileId varchar(255),
  componentId varchar(255),
  groupId integer,
  userId integer,
  CONSTRAINT ownership_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
GRANT ALL ON TABLE ownership TO postgres;
GRANT ALL ON TABLE ownership TO compreg;
grant all on ownership_id_seq to compreg;


CREATE TABLE groupmembership
(
  id serial,
  groupId integer,
  userId integer,
  CONSTRAINT groupmembership_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
GRANT ALL ON TABLE groupmembership TO postgres;
GRANT ALL ON TABLE groupmembership TO compreg;
grant all on groupmembership_id_seq to compreg;
