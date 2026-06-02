CREATE TABLE conversations (
    id VARCHAR(64) PRIMARY KEY,
    title TEXT,
    session_summary TEXT,
    summarized_through_index INT NOT NULL DEFAULT -1,
    user_id VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_conversations_updated_at ON conversations (updated_at DESC);

CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id VARCHAR(64) NOT NULL REFERENCES conversations (id) ON DELETE CASCADE,
    seq INT NOT NULL,
    role VARCHAR(16) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_messages_conversation_seq UNIQUE (conversation_id, seq)
);

CREATE INDEX idx_messages_conversation_id ON messages (conversation_id);
