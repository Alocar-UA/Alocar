-- US01: a resident account is identified by its email (BR2: one account, one subfleet).
ALTER TABLE residents ADD COLUMN email VARCHAR(254);
ALTER TABLE residents ADD CONSTRAINT residents_email_key UNIQUE (email);
