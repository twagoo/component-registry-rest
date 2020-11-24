-- expand schema

ALTER TABLE basedescription 
    ADD recommended boolean DEFAULT 'false' NOT NULL;

CREATE INDEX idx_basedescription_recommended ON basedescription USING btree (recommended);

BEGIN TRANSACTION;

UPDATE basedescription 
    SET content = replace(content, 'http://www.clarin.eu/cmdi/cues/1', 'http://www.clarin.eu/cmd/cues/1')
    WHERE content LIKE '%http://www.clarin.eu/cmdi/cues/1%';

SELECT
    CASE 
        WHEN 0 = count(*) 
        THEN 'Successfully updated cue namespace in component definitions' 
        ELSE 'ERROR while updating cue namespace in component definitions' 
    END as "Update result"
    FROM basedescription 
        WHERE content LIKE '%http://www.clarin.eu/cmdi/cues/1%';

COMMIT;
