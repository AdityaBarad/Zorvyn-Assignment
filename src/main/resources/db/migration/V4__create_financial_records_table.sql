CREATE TABLE financial_records (
    id BIGSERIAL PRIMARY KEY,
    amount DECIMAL(15,2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    category VARCHAR(100) NOT NULL,
    record_date DATE NOT NULL,
    description TEXT,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    deleted_at TIMESTAMP NULL
);

