CREATE TABLE todos (
    id INT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    order INT,
    completed BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
)
