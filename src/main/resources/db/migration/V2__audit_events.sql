CREATE TABLE audit_events (
    id BIGSERIAL PRIMARY KEY,
    conversation_id VARCHAR(64),
    event_type VARCHAR(64) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_events_conversation_created
    ON audit_events (conversation_id, created_at);

CREATE INDEX idx_audit_events_type_created ON audit_events (event_type, created_at);
