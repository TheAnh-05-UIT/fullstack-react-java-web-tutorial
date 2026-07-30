SET @add_tutorials_public_index = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.tables
            WHERE table_schema = DATABASE() AND table_name = 'tutorials'
        ) AND NOT EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'tutorials'
              AND index_name = 'idx_tutorials_public'
        ),
        'CREATE INDEX idx_tutorials_public ON tutorials (status, is_deleted, created_at)',
        'SELECT 1'
    )
);
PREPARE add_tutorials_public_index_statement FROM @add_tutorials_public_index;
EXECUTE add_tutorials_public_index_statement;
DEALLOCATE PREPARE add_tutorials_public_index_statement;

SET @add_projects_public_index = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.tables
            WHERE table_schema = DATABASE() AND table_name = 'projects'
        ) AND NOT EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'projects'
              AND index_name = 'idx_projects_public'
        ),
        'CREATE INDEX idx_projects_public ON projects (status, is_deleted, created_at)',
        'SELECT 1'
    )
);
PREPARE add_projects_public_index_statement FROM @add_projects_public_index;
EXECUTE add_projects_public_index_statement;
DEALLOCATE PREPARE add_projects_public_index_statement;

SET @add_roadmaps_visibility_index = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.tables
            WHERE table_schema = DATABASE() AND table_name = 'roadmaps'
        ) AND NOT EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'roadmaps'
              AND index_name = 'idx_roadmaps_visibility'
        ),
        'CREATE INDEX idx_roadmaps_visibility ON roadmaps (is_deleted)',
        'SELECT 1'
    )
);
PREPARE add_roadmaps_visibility_index_statement FROM @add_roadmaps_visibility_index;
EXECUTE add_roadmaps_visibility_index_statement;
DEALLOCATE PREPARE add_roadmaps_visibility_index_statement;

SET @add_roadmaps_slug_index = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.tables
            WHERE table_schema = DATABASE() AND table_name = 'roadmaps'
        ) AND NOT EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'roadmaps'
              AND index_name = 'idx_roadmaps_slug'
        ),
        'CREATE INDEX idx_roadmaps_slug ON roadmaps (slug)',
        'SELECT 1'
    )
);
PREPARE add_roadmaps_slug_index_statement FROM @add_roadmaps_slug_index;
EXECUTE add_roadmaps_slug_index_statement;
DEALLOCATE PREPARE add_roadmaps_slug_index_statement;

SET @add_devops_active_index = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.tables
            WHERE table_schema = DATABASE() AND table_name = 'devops_phases'
        ) AND NOT EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'devops_phases'
              AND index_name = 'idx_devops_phases_active'
        ),
        'CREATE INDEX idx_devops_phases_active ON devops_phases (active)',
        'SELECT 1'
    )
);
PREPARE add_devops_active_index_statement FROM @add_devops_active_index;
EXECUTE add_devops_active_index_statement;
DEALLOCATE PREPARE add_devops_active_index_statement;
