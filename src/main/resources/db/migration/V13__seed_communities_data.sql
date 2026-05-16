-- Seed the 10 initial communities (idempotent via ON CONFLICT DO NOTHING)
INSERT INTO communities (name, description, category, member_count, post_count, created_at)
VALUES
  (
    'Career & Ambition',
    'Navigate the glass ceiling with grace. Salary transparency, boss battles, job wins, and corporate real talk — all welcome here.',
    'CAREER', 0, 0, NOW()
  ),
  (
    'Mental Wellness & Emotional Support',
    'A gentle space to share, breathe, and heal. Whether you''re thriving or struggling, you belong here. No judgment, only warmth.',
    'MENTAL_HEALTH', 0, 0, NOW()
  ),
  (
    'Beauty, Skincare & Self-Care',
    'Your glow-up starts here. Skincare routines, makeup looks, product reviews, and self-care rituals — spill your secrets.',
    'BEAUTY', 0, 0, NOW()
  ),
  (
    'Fitness, Yoga & Wellness',
    'Move your body, nourish your soul. Workout tips, yoga journeys, nutrition, and the messy real side of staying healthy.',
    'FITNESS', 0, 0, NOW()
  ),
  (
    'Relationships & Dating',
    'Love, heartbreak, red flags, green flags. Honest conversations about dating, friendships, boundaries, and everything in between.',
    'RELATIONSHIPS', 0, 0, NOW()
  ),
  (
    'Fashion & Personal Style',
    'Express yourself. From capsule wardrobes to bold statements — share your fits, styling tips, and fashion finds.',
    'FASHION', 0, 0, NOW()
  ),
  (
    'Finance & Independence',
    'Your money, your freedom. Budgeting, investing, salary negotiation, side hustles — build wealth on your own terms.',
    'FINANCE', 0, 0, NOW()
  ),
  (
    'Lifestyle, Home & Hobbies',
    'The art of living well. Home décor, recipes, books, travel plans, and all the little things that make life beautiful.',
    'LIFESTYLE', 0, 0, NOW()
  ),
  (
    'Motherhood & Family',
    'The unfiltered truth of raising tiny humans. Parenting wins, chaos, guilt, and joy — share it all without shame.',
    'PARENTING', 0, 0, NOW()
  ),
  (
    'Travel, Experiences & City Life',
    'Solo trips, city guides, bucket lists, and spontaneous adventures. Share where you''ve been and where you''re going.',
    'TRAVEL', 0, 0, NOW()
  )
ON CONFLICT (name) DO NOTHING;
