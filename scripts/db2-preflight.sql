-- Read-only DB-2 preflight. It returns aggregate counts only and never emits PII.
SELECT
  (SELECT COUNT(*) FROM (SELECT LOWER(email) FROM users GROUP BY LOWER(email) HAVING COUNT(*) > 1) d)
  + (SELECT COUNT(*) FROM (SELECT slug FROM tutorials GROUP BY slug HAVING COUNT(*) > 1) d)
  + (SELECT COUNT(*) FROM (SELECT slug FROM projects GROUP BY slug HAVING COUNT(*) > 1) d)
  + (SELECT COUNT(*) FROM (SELECT slug FROM roadmaps GROUP BY slug HAVING COUNT(*) > 1) d)
  + (SELECT COUNT(*) FROM (SELECT user_id, content_type, content_key FROM user_learning_progress
      GROUP BY user_id, content_type, content_key HAVING COUNT(*) > 1) d)
  + (SELECT COUNT(*) FROM (SELECT name FROM roles GROUP BY name HAVING COUNT(*) > 1) d)
  + (SELECT COUNT(*) FROM (SELECT project_id, tags FROM project_tags
      GROUP BY project_id, tags HAVING COUNT(*) > 1) d)
  + (SELECT COUNT(*) FROM users WHERE username IS NULL OR email IS NULL OR password IS NULL)
  + (SELECT COUNT(*) FROM tutorials
      WHERE title IS NULL OR slug IS NULL OR content IS NULL OR views IS NULL)
  + (SELECT COUNT(*) FROM projects
      WHERE title IS NULL OR slug IS NULL OR content IS NULL OR views IS NULL)
  + (SELECT COUNT(*) FROM roadmaps WHERE title IS NULL OR slug IS NULL)
  + (SELECT COUNT(*) FROM roadmaps
      WHERE difficulty IS NOT NULL
        AND CAST(difficulty AS CHAR) NOT IN ('0', '1', '2', 'BEGINNER', 'INTERMEDIATE', 'ADVANCED'))
  + (SELECT COUNT(*) FROM projects
      WHERE difficulty IS NOT NULL
        AND difficulty NOT IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED'))
  + (SELECT COUNT(*) FROM tutorials
      WHERE status IS NOT NULL AND status NOT IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
  + (SELECT COUNT(*) FROM projects
      WHERE status IS NOT NULL AND status NOT IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
  + (SELECT COUNT(*) FROM user_learning_progress
      WHERE progress_percent NOT BETWEEN 0 AND 100
         OR content_type NOT IN ('TUTORIAL', 'PROJECT', 'ROADMAP', 'DEVOPS_PHASE')
         OR status NOT IN ('IN_PROGRESS', 'COMPLETED'))
  + (SELECT COUNT(*) FROM refresh_token_sessions
      WHERE CHAR_LENGTH(token_hash) <> 64
         OR (previous_token_hash IS NOT NULL AND CHAR_LENGTH(previous_token_hash) <> 64))
  + (SELECT COUNT(*) FROM roles WHERE name IS NULL)
  + (SELECT COUNT(*) FROM categores WHERE name IS NULL OR slug IS NULL)
  + (SELECT COUNT(*) FROM project_tags WHERE tags IS NULL)
  + (SELECT COUNT(*) FROM devops_phases WHERE title IS NULL OR display_order < 0)
AS violation_count;
