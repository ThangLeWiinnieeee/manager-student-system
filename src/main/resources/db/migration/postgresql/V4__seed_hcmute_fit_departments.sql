-- Official FIT HCMUTE organization data, checked 2026-10-08:
-- https://fit.hcmute.edu.vn/Default.aspx?ArticleId=4e268133-b495-4b0e-9b5c-1d0aa72d5605
INSERT INTO departments (code, name, enabled)
VALUES
    ('CNPM', 'Công nghệ phần mềm', TRUE),
    ('HTTT', 'Hệ thống thông tin', TRUE),
    ('MANM', 'Mạng và An ninh mạng', TRUE),
    ('TTNT', 'Trí tuệ nhân tạo', TRUE)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    enabled = TRUE;
