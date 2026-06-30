ALTER TABLE app_edible_reports DROP COLUMN reason;

CREATE TABLE app_edible_report_reasons (
    report_id INTEGER NOT NULL REFERENCES app_edible_reports(id) ON DELETE CASCADE,
    reason    TEXT    NOT NULL
        CHECK (reason IN (
                          'incorrect_info',
                          'incorrect_nutrients',
                          'incorrect_barcode',
                          'incorrect_type')
            ),
    PRIMARY KEY (report_id, reason)
);
