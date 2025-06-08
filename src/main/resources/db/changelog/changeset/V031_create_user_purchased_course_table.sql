CREATE TABLE IF NOT EXISTS user_schema.user_purchased_course (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    course_id UUID NOT NULL,
    purchased_at TIMESTAMPTZ NOT NULL,
    access_until TIMESTAMPTZ NOT NULL
);
