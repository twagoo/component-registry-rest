-- expand schema

ALTER TABLE basedescription 
    ADD recommended boolean DEFAULT 'false' NOT NULL;

CREATE INDEX idx_basedescription_recommended ON basedescription USING btree (recommended);
