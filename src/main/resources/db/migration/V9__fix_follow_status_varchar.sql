-- Convert status column from PostgreSQL enum to VARCHAR so Hibernate can map it cleanly
ALTER TABLE follows
    ALTER COLUMN status TYPE VARCHAR(20) USING status::VARCHAR;

ALTER TABLE follows
    ALTER COLUMN status SET DEFAULT 'PENDING';
