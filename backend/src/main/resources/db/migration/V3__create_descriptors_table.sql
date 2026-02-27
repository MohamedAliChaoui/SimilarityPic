CREATE TABLE IF NOT EXISTS descriptors (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(255) NOT NULL,
    histogram_data FLOAT ARRAY,
    image_id BIGINT,
    FOREIGN KEY (image_id) REFERENCES images(id)
); 