CREATE TABLE saved_images (
    image_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (image_id, user_id),
    FOREIGN KEY (image_id) REFERENCES images(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
); 