delete from component_description cascade;
delete from profile_description cascade;
delete from xml_content cascade;
delete from comments cascade;
delete from registry_user cascade;
alter sequence registry_user_id_seq RESTART WITH 1;
alter sequence xml_content_id_seq RESTART WITH 1;
alter sequence profile_description_id_seq RESTART WITH 1;
alter sequence component_description_id_seq RESTART WITH 1;
alter sequence comments_id_seq RESTART WITH 1;