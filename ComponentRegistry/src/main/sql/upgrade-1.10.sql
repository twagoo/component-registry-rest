---
--- This scripts adds columns to the database that are required for
--- the component registry REST service versions 1.10 and up.
---
--- Apply only when upgrading from an existing version lower than 1.11

DROP TABLE comments;
alter table profile_description add column show_in_editor boolean DEFAULT true NOT NULL;

CREATE TABLE comments
(
  id SERIAL NOT NULL,
  comments text NOT NULL,
  comment_date timestamp with time zone NOT NULL,
  profile_description_id character varying,
  component_description_id character varying,
  user_id integer NOT NULL

);


CREATE INDEX fki_comments_fk_user ON comments USING btree (user_id);
CREATE INDEX fki_comments_profile_id ON comments USING btree (profile_description_id);
CREATE INDEX fki_comments_component_id ON comments USING btree (component_description_id);
ALTER TABLE ONLY comments ADD CONSTRAINT comments_id_pkey PRIMARY KEY(id);
ALTER TABLE ONLY comments ADD CONSTRAINT comments_user FOREIGN KEY (user_id) REFERENCES registry_user(id);
ALTER TABLE ONLY comments ADD CONSTRAINT comments_profile_id FOREIGN KEY (profile_description_id) REFERENCES profile_description(profile_id);
ALTER TABLE ONLY comments ADD CONSTRAINT comments_component_id FOREIGN KEY (component_description_id) REFERENCES component_description(component_id);