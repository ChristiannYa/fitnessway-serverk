CREATE TABLE app_edible_report_updates
(
    report_id          INTEGER NOT NULL REFERENCES app_edible_reports (id) ON DELETE CASCADE PRIMARY KEY,
    name               TEXT,
    brand              TEXT,
    amount_per_serving NUMERIC(12, 4),
    serving_unit       serving_unit,
    edible_type        edible_type,
    barcode            TEXT
);

CREATE TABLE app_edible_report_update_nutrients
(
    report_id   INTEGER        NOT NULL REFERENCES app_edible_report_updates (report_id) ON DELETE CASCADE,
    nutrient_id INTEGER        NOT NULL REFERENCES nutrients (id) ON DELETE CASCADE,
    amount      NUMERIC(12, 4) NOT NULL,
    PRIMARY KEY (report_id, nutrient_id)
);