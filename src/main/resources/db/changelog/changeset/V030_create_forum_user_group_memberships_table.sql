CREATE TABLE IF NOT EXISTS user_schema.forum_user_group_membership (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    group_id UUID NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL,
    UNIQUE (user_id, group_id)
);