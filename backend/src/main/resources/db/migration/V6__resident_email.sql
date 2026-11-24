-- US01: a resident account is identified by its email (BR2: one account, one subfleet).
-- Emails are stored in lower case by the application, so the unique key is case-insensitive.
ALTER TABLE residents ADD COLUMN email VARCHAR(254);

-- Synthetic addresses for the seeded residents (fictional data).
UPDATE residents SET email = lower(replace(name, ' ', '.')) || '@exemplo.pt';

ALTER TABLE residents ALTER COLUMN email SET NOT NULL;
ALTER TABLE residents ADD CONSTRAINT residents_email_key UNIQUE (email);
