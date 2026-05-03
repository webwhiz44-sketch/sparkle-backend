# Backend Spec — Radiant Stories Feature

## Overview
Radiant Stories is the long-form written content type on Sparkle & Spill.
Users write personal essays, experiences, or narratives with a title, cover image, and body text.
Comments and likes follow the exact same pattern as Posts.

---

## 1. Database Migration — `V5__stories.sql`

```sql
-- Stories table
CREATE TABLE stories (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT NOT NULL,
    title            VARCHAR(300) NOT NULL,
    body             TEXT NOT NULL,
    cover_image_url  VARCHAR(500),
    tags             VARCHAR(100)[],
    like_count       INT DEFAULT 0,
    comment_count    INT DEFAULT 0,
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Extend existing likes table to support story likes
ALTER TABLE likes ADD COLUMN story_id BIGINT REFERENCES stories(id) ON DELETE CASCADE;

-- Extend existing comments table to support story comments
ALTER TABLE comments ADD COLUMN story_id BIGINT REFERENCES stories(id) ON DELETE CASCADE;

-- Indexes
CREATE INDEX idx_stories_user_id    ON stories(user_id);
CREATE INDEX idx_stories_created_at ON stories(created_at DESC);
CREATE INDEX idx_likes_story_id     ON likes(story_id);
CREATE INDEX idx_comments_story_id  ON comments(story_id);
```

---

## 2. New Files to Create

### `model/entity/Story.java`
```java
@Entity
@Table(name = "stories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Story {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(columnDefinition = "varchar(100)[]")
    private List<String> tags;

    @Column(name = "like_count") @Builder.Default
    private Integer likeCount = 0;

    @Column(name = "comment_count") @Builder.Default
    private Integer commentCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

---

### `model/dto/response/StoryResponse.java`
```java
@Data @Builder
public class StoryResponse {
    private Long id;
    private UserResponse author;
    private String title;
    private String body;
    private String coverImageUrl;
    private List<String> tags;
    private int likeCount;
    private int commentCount;
    private int readTimeMinutes;   // computed: Math.max(1, wordCount / 200)
    private boolean likedByMe;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StoryResponse from(Story story) {
        int wordCount = story.getBody().split("\\s+").length;
        return StoryResponse.builder()
                .id(story.getId())
                .author(UserResponse.from(story.getUser()))
                .title(story.getTitle())
                .body(story.getBody())
                .coverImageUrl(story.getCoverImageUrl())
                .tags(story.getTags())
                .likeCount(story.getLikeCount())
                .commentCount(story.getCommentCount())
                .readTimeMinutes(Math.max(1, wordCount / 200))
                .createdAt(story.getCreatedAt())
                .updatedAt(story.getUpdatedAt())
                .build();
    }
}
```

---

### `model/dto/request/CreateStoryRequest.java`
```java
@Data
public class CreateStoryRequest {
    @NotBlank
    @Size(max = 300)
    private String title;

    @NotBlank
    private String body;

    private String coverImageUrl;

    private List<String> tags = new ArrayList<>();
}
```

---

### `model/dto/request/UpdateStoryRequest.java`
```java
@Data
public class UpdateStoryRequest {
    @Size(max = 300)
    private String title;

    private String body;
    private String coverImageUrl;
    private List<String> tags;
}
```

---

### `repository/StoryRepository.java`
```java
public interface StoryRepository extends JpaRepository<Story, Long> {
    Page<Story> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Story> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
```

---

### `service/StoryService.java`

**Methods:**
```java
StoryResponse createStory(Long userId, CreateStoryRequest request)
PagedResponse<StoryResponse> getFeed(Long userId, int page, int size)
StoryResponse getStory(Long storyId, Long userId)
StoryResponse updateStory(Long storyId, Long userId, UpdateStoryRequest request)
void deleteStory(Long storyId, Long userId)
void likeStory(Long storyId, Long userId)
void unlikeStory(Long storyId, Long userId)
```

**`likeStory` logic** (mirrors `PostService.likePost`):
```java
// Check no duplicate like (query likes by user_id + story_id)
// Insert into likes(user_id, story_id)
// Increment stories.like_count
```

**`enrichWithLikeStatus` for feed** — query likes table by `userId + storyId` to set `likedByMe`.

---

### `controller/StoryController.java`
```java
@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
@Tag(name = "Radiant Stories")
public class StoryController {
    ...
}
```

---

## 3. Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/stories` | ✅ Required | Create a story |
| `GET` | `/api/stories` | Optional | Paginated feed, sorted by newest |
| `GET` | `/api/stories/{id}` | Optional | Single story with full body |
| `PUT` | `/api/stories/{id}` | ✅ Required | Edit own story |
| `DELETE` | `/api/stories/{id}` | ✅ Required | Delete own story |
| `POST` | `/api/stories/{id}/like` | ✅ Required | Like a story |
| `DELETE` | `/api/stories/{id}/like` | ✅ Required | Unlike a story |
| `GET` | `/api/stories/{id}/comments` | Optional | Get comments, paginated |
| `POST` | `/api/stories/{id}/comments` | ✅ Required | Post a comment |

---

## 4. Files to Modify

### `model/entity/Like.java`
Add field:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "story_id")
private Story story;
```

### `model/entity/Comment.java`
Add field:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "story_id")
private Story story;
```

### `repository/LikeRepository.java`
Add method:
```java
boolean existsByUserIdAndStoryId(Long userId, Long storyId);
void deleteByUserIdAndStoryId(Long userId, Long storyId);
```

### `service/CommentService.java`
Add two methods (mirrors `commentOnPost`):
```java
CommentResponse commentOnStory(Long storyId, Long userId, CreateCommentRequest request)
PagedResponse<CommentResponse> getStoryComments(Long storyId, Long userId, int page, int size)
```

### `controller/CommentController.java`
Add two endpoints (mirrors post comment endpoints):
```java
@PostMapping("/api/stories/{storyId}/comments")
@GetMapping("/api/stories/{storyId}/comments")
```

---

## 5. Request / Response Examples

### `POST /api/stories`
**Request:**
```json
{
  "title": "How I Left a Toxic Relationship and Found Myself",
  "body": "It was a Tuesday when I finally...",
  "coverImageUrl": "https://storage.googleapis.com/sparkle/.../cover.jpg",
  "tags": ["relationships", "healing", "selfgrowth"]
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 42,
    "author": { "id": 7, "displayName": "Priya K.", "profileImageUrl": null },
    "title": "How I Left a Toxic Relationship and Found Myself",
    "body": "It was a Tuesday when I finally...",
    "coverImageUrl": "https://storage.googleapis.com/...",
    "tags": ["relationships", "healing", "selfgrowth"],
    "likeCount": 0,
    "commentCount": 0,
    "readTimeMinutes": 4,
    "likedByMe": false,
    "createdAt": "2026-05-01T14:22:00",
    "updatedAt": "2026-05-01T14:22:00"
  }
}
```

### `GET /api/stories?page=0&size=10`
Returns `PagedResponse<StoryResponse>` — same `content/page/size/totalElements/totalPages/last` shape as all other feeds.
Note: `body` field is included (frontend truncates for card previews).

---

## 6. Implementation Order (suggested)

1. `V5__stories.sql` migration
2. `Story.java` entity
3. Update `Like.java` + `Comment.java` with `story` field
4. `StoryRepository.java`
5. `StoryResponse.java`, `CreateStoryRequest.java`, `UpdateStoryRequest.java`
6. `StoryService.java` — CRUD + like/unlike
7. `StoryController.java`
8. Update `LikeRepository.java` with story methods
9. Update `CommentService.java` + `CommentController.java` with story comment endpoints
10. Test all endpoints
