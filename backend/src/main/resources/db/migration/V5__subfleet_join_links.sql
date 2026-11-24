-- US01: each subfleet has a join link; residents who sign up through it belong to that subfleet (BR2).
ALTER TABLE subfleets ADD COLUMN join_token VARCHAR(64);
UPDATE subfleets SET join_token = gen_random_uuid()::text;
ALTER TABLE subfleets ALTER COLUMN join_token SET NOT NULL;
ALTER TABLE subfleets ADD CONSTRAINT subfleets_join_token_key UNIQUE (join_token);

ALTER TABLE subfleets ADD COLUMN join_link_active BOOLEAN NOT NULL DEFAULT TRUE;
