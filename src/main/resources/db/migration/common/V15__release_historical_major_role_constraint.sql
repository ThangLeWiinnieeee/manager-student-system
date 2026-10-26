-- major_id is historical data; role changes must use the current department mapping.
ALTER TABLE users DROP CONSTRAINT ck_users_student_major;
