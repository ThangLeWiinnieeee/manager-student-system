-- shortcut: H2 cannot run PostgreSQL ON CONFLICT; keep this test seed aligned with production V4.
MERGE INTO departments (code, name, enabled) KEY (code)
VALUES
    ('CNPM', 'Công nghệ phần mềm', TRUE),
    ('HTTT', 'Hệ thống thông tin', TRUE),
    ('MANM', 'Mạng và An ninh mạng', TRUE),
    ('TTNT', 'Trí tuệ nhân tạo', TRUE);
