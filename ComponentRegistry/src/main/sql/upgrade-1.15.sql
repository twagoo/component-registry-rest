ALTER TABLE profile_description RENAME TO persistentcomponent;

alter table persistentcomponent
rename column profile_id to component_id;

insert into persistentcomponent
select * from component_description;

update comments set component_description_id = profile_description_id where component_description_id is null;
alter table comments rename column component_description_id to component_id;
alter table comments drop profile_description_id;

drop table component_description cascade;

update ownership set componentid = profileid where componentid is null;

alter table ownership drop column profileid;