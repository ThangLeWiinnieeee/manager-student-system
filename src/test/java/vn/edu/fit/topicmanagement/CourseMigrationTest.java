package vn.edu.fit.topicmanagement;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class CourseMigrationTest {
    @Test
    void upgradePreservesHistoryAndCreatesIndependentGroupAssessmentRecords() throws Exception {
        String url = "jdbc:h2:mem:course_upgrade;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;"
                + "INIT=CREATE DOMAIN IF NOT EXISTS TIMESTAMPTZ AS TIMESTAMP WITH TIME ZONE";
        String[] locations = {"classpath:db/migration/common", "classpath:db/migration/h2"};
        try (var connection = DriverManager.getConnection(url, "sa", ""); var sql = connection.createStatement()) {
            Flyway.configure().dataSource(url, "sa", "").locations(locations).target("12").load().migrate();
            sql.execute("INSERT INTO majors(id,code,name) VALUES(99,'MIGRATION','Department history')");
            sql.execute("INSERT INTO users(id,email,password_hash,full_name,role,department_id) VALUES(99,'gv@migration.test','unchanged','Lecturer','LECTURER',99)");
            sql.execute("INSERT INTO users(id,email,password_hash,full_name,role,major_id) VALUES(100,'sv@migration.test','unchanged','Student','STUDENT',99)");
            sql.execute("""
                    INSERT INTO registration_periods(id,name,round_type,gv_start,gv_end,sv_start,sv_end)
                    VALUES(99,'Legacy round','MON_HOC',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
                    """);
            sql.execute("INSERT INTO topics(id,title,status,round_id,major_id,created_by_id) VALUES(99,'Legacy topic','PUBLISHED',99,99,99)");
            sql.execute("INSERT INTO topic_advisors(topic_id,advisor_id) VALUES(99,99)");
            sql.execute("INSERT INTO student_groups(id,name,round_id,leader_id) VALUES(99,'A',99,100),(100,'B',99,NULL),(101,'C',99,NULL)");
            sql.execute("INSERT INTO student_group_members(group_id,student_id) VALUES(99,100)");
            sql.execute("INSERT INTO group_topic_registrations(id,group_id,topic_id,status) VALUES(99,99,99,'APPROVED'),(100,100,99,'APPROVED'),(101,101,99,'REJECTED')");
            sql.execute("INSERT INTO councils(id,name,round_id,status) VALUES(99,'Legacy council',99,'ACTIVE')");
            sql.execute("INSERT INTO topic_assignments(topic_id,council_id,reviewer_id) VALUES(99,99,99)");
            sql.execute("INSERT INTO evaluations(topic_id,evaluator_id,evaluation_type,total_score,status) VALUES(99,99,'REVIEWER',8,'SUBMITTED')");
            sql.execute("INSERT INTO topic_final_results(topic_id,council_id,final_score,president_confirmed,published) VALUES(99,99,8.7,TRUE,TRUE)");

            Flyway.configure().dataSource(url, "sa", "").locations(locations).load().migrate();

            try (var rows = sql.executeQuery("SELECT academic_department_id,password_hash FROM users WHERE id IN(99,100)")) {
                int count = 0;
                while (rows.next()) { count++; assertEquals(99, rows.getLong(1)); assertEquals("unchanged", rows.getString(2)); }
                assertEquals(2, count);
            }
            for (String table : new String[]{"group_assignments", "group_evaluations", "group_results"}) {
                try (var rows = sql.executeQuery("SELECT COUNT(*) FROM " + table)) { rows.next(); assertEquals(2, rows.getInt(1)); }
            }
            try (var rows = sql.executeQuery("SELECT legacy_assessment FROM group_assignments")) {
                while (rows.next()) assertTrue(rows.getBoolean(1));
            }
            try (var rows = sql.executeQuery("SELECT course_id,student_id FROM course_participants")) {
                assertTrue(rows.next()); assertEquals(99, rows.getLong(1)); assertEquals(100, rows.getLong(2)); assertFalse(rows.next());
            }
            assertEquals(1, sql.executeUpdate("UPDATE users SET role='LECTURER',academic_department_id=NULL WHERE id=100"));
            try (var rows = sql.executeQuery("SELECT major_id FROM users WHERE id=100")) {
                assertTrue(rows.next()); assertEquals(99, rows.getLong(1));
            }
            for (String table : new String[]{"topic_assignments", "evaluations", "topic_final_results", "topic_advisors"}) {
                try (var rows = sql.executeQuery("SELECT COUNT(*) FROM " + table)) { rows.next(); assertEquals(1, rows.getInt(1)); }
            }
            try (var rows = sql.executeQuery("SELECT final_score,published FROM group_results")) {
                while (rows.next()) { assertEquals(8.7, rows.getDouble(1)); assertTrue(rows.getBoolean(2)); }
            }
            try (var rows = sql.executeQuery("SELECT registration_mode,max_members,class_code FROM registration_periods WHERE id=99")) {
                assertTrue(rows.next()); assertEquals("SHARED", rows.getString(1)); assertEquals(5, rows.getInt(2)); assertEquals("LEGACY", rows.getString(3));
            }
            try (var rows = sql.executeQuery("SELECT w.name,w.round_type,w.gv_start,w.sv_end FROM registration_periods c JOIN registration_windows w ON w.id=c.period_id WHERE c.id=99")) {
                assertTrue(rows.next()); assertEquals("Legacy round", rows.getString(1));
                assertEquals("MON_HOC", rows.getString(2)); assertNotNull(rows.getTimestamp(3)); assertNotNull(rows.getTimestamp(4));
            }
            assertThrows(SQLException.class, () -> sql.execute("INSERT INTO student_group_members(group_id,student_id,round_id) VALUES(100,100,99)"));
            sql.execute("INSERT INTO registration_periods(id,period_id,course_name,class_code) SELECT 102,period_id,'Other course','OTHER' FROM registration_periods WHERE id=99");
            sql.execute("INSERT INTO student_groups(id,name,round_id,leader_id) VALUES(102,'Other class group',102,100)");
            assertEquals(1, sql.executeUpdate("INSERT INTO student_group_members(group_id,student_id,round_id) VALUES(102,100,102)"));
            sql.execute("UPDATE group_topic_registrations SET exclusive_topic_id=99 WHERE id=99");
            assertThrows(SQLException.class, () -> sql.execute("UPDATE group_topic_registrations SET exclusive_topic_id=99 WHERE id=100"));
            sql.execute("UPDATE group_evaluations SET total_score=7 WHERE registration_id=100");
            try (var rows = sql.executeQuery("SELECT total_score FROM group_evaluations WHERE registration_id=99")) {
                assertTrue(rows.next()); assertEquals(8, rows.getInt(1));
            }
        }
    }
}
