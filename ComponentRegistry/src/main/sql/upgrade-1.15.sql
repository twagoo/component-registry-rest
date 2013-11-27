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