CREATE TABLE IF NOT EXISTS user_schema.forum_user_group (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    course_id UUID NOT NULL,
    title VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);