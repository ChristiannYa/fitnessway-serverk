ALTER TABLE app_edible_report_reasons DROP CONSTRAINT app_edible_report_reasons_reason_check;

ALTER TABLE app_edible_report_reasons
ADD CONSTRAINT app_edible_report_reasons_reason_check
    CHECK (reason IN ('incorrect_info',
                      'incorrect_nutrients',
                      'incorrect_barcode',
                      'incorrect_type',
                      'inappropriate_info',
                      'invalid_info')
    );