ALTER TABLE anonymous_posts
    ADD COLUMN image_url TEXT,
    ALTER COLUMN content DROP NOT NULL;
