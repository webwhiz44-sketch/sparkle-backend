CREATE TABLE stories (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    title           VARCHAR(300) NOT NULL,
    body            TEXT NOT NULL,
    cover_image_url VARCHAR(500),
    tags            VARCHAR(100)[],
    like_count      INT DEFAULT 0,
    comment_count   INT DEFAULT 0,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

ALTER TABLE likes    ADD COLUMN story_id BIGINT REFERENCES stories(id) ON DELETE CASCADE;
ALTER TABLE comments ADD COLUMN story_id BIGINT REFERENCES stories(id) ON DELETE CASCADE;

CREATE INDEX idx_stories_user_id    ON stories(user_id);
CREATE INDEX idx_stories_created_at ON stories(created_at DESC);
CREATE INDEX idx_likes_story_id     ON likes(story_id);
CREATE INDEX idx_comments_story_id  ON comments(story_id);
