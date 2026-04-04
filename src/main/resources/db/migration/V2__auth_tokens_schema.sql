CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked BIT NOT NULL DEFAULT b'0',
    utilisateur_id BIGINT NOT NULL,
    CONSTRAINT uk_refresh_tokens_token UNIQUE (token),
    CONSTRAINT fk_refresh_tokens_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id_utilisateur)
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    expires_at DATETIME NOT NULL,
    used BIT NOT NULL DEFAULT b'0',
    utilisateur_id BIGINT NOT NULL,
    CONSTRAINT uk_password_reset_tokens_token UNIQUE (token),
    CONSTRAINT fk_password_reset_tokens_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur (id_utilisateur)
);

CREATE INDEX idx_refresh_tokens_utilisateur_id ON refresh_tokens (utilisateur_id);
CREATE INDEX idx_password_reset_tokens_utilisateur_id ON password_reset_tokens (utilisateur_id);