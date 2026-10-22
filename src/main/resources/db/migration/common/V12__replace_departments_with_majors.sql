ALTER TABLE departments RENAME TO majors;
ALTER TABLE users ADD COLUMN major_id BIGINT REFERENCES majors(id);

-- Keep users.department_id as historical data; the application now uses major_id.
UPDATE users SET major_id = department_id WHERE role = 'STUDENT';
ALTER TABLE users ADD CONSTRAINT ck_users_student_major
    CHECK (role = 'STUDENT' OR major_id IS NULL);
CREATE INDEX idx_users_major ON users(major_id);

ALTER TABLE topics RENAME COLUMN department_id TO major_id;
ALTER TABLE topics ALTER COLUMN major_id DROP NOT NULL;
