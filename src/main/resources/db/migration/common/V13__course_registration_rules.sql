ALTER TABLE majors RENAME TO departments;
ALTER TABLE users ADD COLUMN academic_department_id BIGINT REFERENCES departments(id);
UPDATE users SET academic_department_id = COALESCE(major_id, department_id);
ALTER TABLE topics RENAME COLUMN major_id TO department_id;

ALTER TABLE registration_periods ADD COLUMN course_name VARCHAR(150);
ALTER TABLE registration_periods ADD COLUMN class_code VARCHAR(100);
ALTER TABLE registration_periods ADD COLUMN lecturer_id BIGINT REFERENCES users(id);
ALTER TABLE registration_periods ADD COLUMN department_id BIGINT REFERENCES departments(id);
ALTER TABLE registration_periods ADD COLUMN registration_mode VARCHAR(20) NOT NULL DEFAULT 'SHARED';
ALTER TABLE registration_periods ADD COLUMN max_members INTEGER NOT NULL DEFAULT 5;
ALTER TABLE registration_periods ADD CONSTRAINT ck_round_mode CHECK (registration_mode IN ('EXCLUSIVE', 'SELF_PROPOSED', 'SHARED'));
ALTER TABLE registration_periods ADD CONSTRAINT ck_round_members CHECK (max_members BETWEEN 1 AND 5);
UPDATE registration_periods SET course_name = LEFT(name, 150), class_code = 'LEGACY';

ALTER TABLE topics ADD COLUMN proposed_group_id BIGINT REFERENCES student_groups(id);
ALTER TABLE group_topic_registrations ADD COLUMN exclusive_topic_id BIGINT UNIQUE REFERENCES topics(id);
ALTER TABLE group_topic_registrations ADD CONSTRAINT ck_exclusive_registration
    CHECK (exclusive_topic_id IS NULL OR (exclusive_topic_id = topic_id AND status = 'APPROVED'));

ALTER TABLE student_groups ADD CONSTRAINT uq_group_round UNIQUE (id, round_id);
ALTER TABLE student_group_members ADD COLUMN round_id BIGINT;
UPDATE student_group_members SET round_id = (SELECT g.round_id FROM student_groups g WHERE g.id = group_id);
ALTER TABLE student_group_members ALTER COLUMN round_id SET NOT NULL;
ALTER TABLE student_group_members ADD CONSTRAINT fk_member_round FOREIGN KEY (group_id, round_id) REFERENCES student_groups(id, round_id);
ALTER TABLE student_group_members ADD CONSTRAINT uq_student_round UNIQUE (student_id, round_id);
