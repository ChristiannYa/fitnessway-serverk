CREATE TABLE app_edible_reports
(
    id          SERIAL PRIMARY KEY,
    edible_id   INTEGER     NOT NULL REFERENCES app_edibles (id) ON DELETE CASCADE,
    reported_by UUID        REFERENCES users (id) ON DELETE SET NULL,
    reason      TEXT        NOT NULL
        CHECK (reason IN ('incorrect_info',
                          'incorrect_nutrients',
                          'incorrect_barcode')
            ),
    notes       TEXT CHECK ( LENGTH(notes) <= 100 ),
    status      TEXT        NOT NULL DEFAULT 'pending'
        CHECK (status IN ('pending',
                          'reviewed')
            ),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    reviewed_at TIMESTAMPTZ,
    reviewed_by UUID        REFERENCES users (id) ON DELETE SET NULL
);
