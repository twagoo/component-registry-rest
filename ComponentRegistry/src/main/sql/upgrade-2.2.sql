-- expand schema

CREATE TYPE item_status as ENUM('DEVELOPMENT', 'PRODUCTION', 'DEPRECATED');

ALTER TABLE basedescription 
    ADD status item_status, 
    ADD derivedfrom integer, 
    ADD successor integer;

-- set status

UPDATE basedescription SET status = 'PRODUCTION' WHERE is_deleted = false AND content like '%<Status>production</Status>%';
UPDATE basedescription SET status = 'DEVELOPMENT' WHERE is_deleted = false AND content like '%<Status>development</Status>%';
UPDATE basedescription SET status = 'DEPRECATED' WHERE is_deleted = true OR content like '%<Status>deprecated</Status>%';

-- set derived from

UPDATE basedescription AS bd
    SET derivedfrom = 
        (SELECT id 
            FROM basedescription 
            WHERE component_id = regexp_replace(bd.content, '.*(<DerivedFrom>)(.*)(</DerivedFrom>).*', '\2'))
    WHERE content LIKE '%<DerivedFrom>%';

-- no need to set successor, feature not available before CompReg v2.2
