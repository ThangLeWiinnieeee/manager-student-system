ALTER TABLE registration_windows ADD COLUMN gv_notice_sent BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE registration_windows ADD COLUMN sv_notice_sent BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE notifications ADD COLUMN automatic BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE notifications ALTER COLUMN created_by_id DROP NOT NULL;
ALTER TABLE notifications ADD CONSTRAINT ck_notification_creator CHECK (automatic OR created_by_id IS NOT NULL);
