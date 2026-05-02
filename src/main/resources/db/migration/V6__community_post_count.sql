ALTER TABLE communities ADD COLUMN post_count INT DEFAULT 0;

-- Backfill existing communities with current post counts
UPDATE communities c
SET post_count = (SELECT COUNT(*) FROM posts p WHERE p.community_id = c.id);
