package vn.edu.fit.topicmanagement.major;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.DriverManager;
import java.sql.SQLException;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class MajorMigrationTest {
    @Test
    void upgradePreservesAccountsTopicsAdvisorsAndHistoricalLinks() throws Exception {
        String url = "jdbc:h2:mem:major_upgrade;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;"
                + "INIT=CREATE DOMAIN IF NOT EXISTS TIMESTAMPTZ AS TIMESTAMP WITH TIME ZONE";
        String[] locations = {"classpath:db/migration/common", "classpath:db/migration/h2"};
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var sql = connection.createStatement()) {
            Flyway.configure().dataSource(url, "sa", "").locations(locations).target("11").load().migrate();
            sql.execute("INSERT INTO departments (id, code, name) VALUES (99, 'MIGRATION', 'Migration major')");
            for (String role : new String[]{"STUDENT", "LECTURER", "DEAN", "ADMIN"}) {
                sql.execute("INSERT INTO users (email, password_hash, full_name, role, department_id) "
                        + "VALUES ('" + role + "@migration.test', 'unchanged-hash', 'Migration user', '" + role + "', 99)");
            }
            sql.execute("""
                    INSERT INTO registration_periods (id, name, round_type, gv_start, gv_end, sv_start, sv_end)
                    VALUES (99, 'Migration round', 'MON_HOC', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
                            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """);
            sql.execute("""
                    INSERT INTO topics (id, title, status, round_id, department_id, created_by_id)
                    SELECT 99, 'Migration topic', 'DRAFT', 99, 99, id FROM users WHERE email = 'LECTURER@migration.test'
                    """);
            sql.execute("""
                    INSERT INTO topic_advisors (topic_id, advisor_id)
                    SELECT 99, id FROM users WHERE email = 'LECTURER@migration.test'
                    """);

            Flyway.configure().dataSource(url, "sa", "").locations(locations).load().migrate();

            try (var rows = sql.executeQuery("SELECT name FROM majors WHERE id = 99")) {
                assertTrue(rows.next());
                assertEquals("Migration major", rows.getString(1));
            }
            int count = 0;
            try (var rows = sql.executeQuery("SELECT role, department_id, major_id, password_hash FROM users WHERE department_id = 99")) {
                while (rows.next()) {
                    count++;
                    assertEquals(99, rows.getLong("department_id"));
                    assertEquals("unchanged-hash", rows.getString("password_hash"));
                    assertEquals(rows.getString("role").equals("STUDENT") ? 99L : null, rows.getObject("major_id"));
                }
            }
            assertEquals(4, count);
            try (var rows = sql.executeQuery("""
                    SELECT t.major_id, u.email FROM topics t
                    JOIN topic_advisors a ON a.topic_id = t.id JOIN users u ON u.id = a.advisor_id WHERE t.id = 99
                    """)) {
                assertTrue(rows.next());
                assertEquals(99, rows.getLong(1));
                assertEquals("LECTURER@migration.test", rows.getString(2));
            }
            assertThrows(SQLException.class, () -> sql.executeUpdate(
                    "UPDATE users SET major_id = 99 WHERE email = 'LECTURER@migration.test'"));
            assertEquals(1, sql.executeUpdate("UPDATE topics SET major_id = NULL WHERE id = 99"));
        }
    }
}
