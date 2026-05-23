CREATE TABLE app_edible_barcodes
(
    barcode    VARCHAR(20) PRIMARY KEY,
    edible_id  INTEGER REFERENCES app_edibles (id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);