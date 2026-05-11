package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/** Drops legacy {@code documents.summary} if present (JPA entity no longer maps it). */
public class V1__drop_documents_summary_column extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection conn = context.getConnection();
        boolean exists = false;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT COUNT(*) FROM information_schema.COLUMNS "
                             + "WHERE TABLE_SCHEMA = DATABASE() "
                             + "AND TABLE_NAME = 'documents' "
                             + "AND COLUMN_NAME = 'summary'"
             )) {
            if (rs.next()) {
                exists = rs.getInt(1) > 0;
            }
        }
        if (exists) {
            try (Statement st = conn.createStatement()) {
                st.execute("ALTER TABLE documents DROP COLUMN summary");
            }
        }
    }
}
