---
--- This scripts adds columns to the database that are required for
--- the component registry REST service versions 1.10 and up.
---
--- Apply only when upgrading from an existing version lower than 1.10
---

alter table profile_description add column show_in_editor boolean DEFAULT true NOT NULL;
