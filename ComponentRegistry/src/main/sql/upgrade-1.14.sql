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


-- originally from upgrade-1.15.sql, was merged back into 1.14 (November 2014)

ALTER TABLE profile_description RENAME TO persistentcomponents;

alter table persistentcomponents
 rename column profile_id to component_id;

insert into persistentcomponents(user_id, content_id, is_public, is_deleted, component_id, name, description, registration_date, 
 creator_name, domain_name, group_name, href, show_in_editor)
 select user_id, content_id,is_public, is_deleted, component_id, name, description, registration_date, 
 creator_name, domain_name,  group_name, href, true
 from component_description;

alter table comments drop constraint comments_component_id;

update comments set component_description_id = profile_description_id where component_description_id is null;

alter table comments rename column component_description_id to component_id;
alter table comments drop profile_description_id;

drop table component_description cascade;

update ownership set componentid = profileid where componentid is null;

alter table ownership drop column profileid;

alter table persistentcomponents
add column content text NOT NULL default '';

update persistentcomponents set content = 
(select content 
 from xml_content
 where xml_content.id = persistentcomponents.content_id
 );

drop table xml_content cascade;

alter table persistentcomponents
drop column content_id;

-- originally from upgrade-1.15.1.sql, was merged back into 1.14 (November 2014)

alter table persistentcomponents rename to basedescription;
alter sequence profile_description_id_seq rename to basedescription_id_seq;
ALTER INDEX profile_description_pkey RENAME TO basedescription_pkey;
alter table basedescription add constraint constraint_basedescription_unique_id unique(id);
alter table basedescription drop constraint profile_description_profile_id_key;
alter table basedescription add constraint fk_basedescription_user_id foreign key(user_id) references registry_user (id) match full;
alter table basedescription drop constraint profile_user;
drop index fki_profile_user;

create index idx_basedescription_id on basedescription(id);
create index idx_user_id on basedescription(user_id);
create index idx_is_deleted on basedescription(is_deleted);
create index idx_is_public on basedescription(is_public);
create index idx_component_id on basedescription(component_id);
