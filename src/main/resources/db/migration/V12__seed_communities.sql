-- Add new topic category enum values
-- Must be in a separate migration from any INSERT that uses these values
ALTER TYPE topic_category ADD VALUE IF NOT EXISTS 'BEAUTY';
ALTER TYPE topic_category ADD VALUE IF NOT EXISTS 'FITNESS';
ALTER TYPE topic_category ADD VALUE IF NOT EXISTS 'FASHION';
ALTER TYPE topic_category ADD VALUE IF NOT EXISTS 'FINANCE';
ALTER TYPE topic_category ADD VALUE IF NOT EXISTS 'TRAVEL';
