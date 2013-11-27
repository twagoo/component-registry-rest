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
