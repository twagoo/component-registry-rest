-- expand schema

ALTER TABLE basedescription 
    ADD status integer, 
    ADD derivedfrom character varying, 
    ADD successor character varying;


CREATE INDEX idx_basedescription_status ON basedescription USING btree (status);

-- set status (maps to clarin.cmdi.componentregistry.model.ComponentStatus enum)

-- 0   DEVELOPMENT("development"),
UPDATE basedescription SET status = '0' WHERE is_deleted = false AND content like '%<Status>development</Status>%';
-- 1   PRODUCTION("production"),
UPDATE basedescription SET status = '1' WHERE is_deleted = false AND content like '%<Status>production</Status>%';
-- 2   DEPRECATED("deprecated");
UPDATE basedescription SET status = '2' WHERE is_deleted = true OR content like '%<Status>deprecated</Status>%';

-- set derived from

UPDATE basedescription AS bd
    SET derivedfrom = regexp_replace(bd.content, '.*(<DerivedFrom>)(.*)(</DerivedFrom>).*', '\2')
    WHERE content LIKE '%<DerivedFrom>%';

-- no need to set successor, feature not available before CompReg v2.2
