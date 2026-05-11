package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/** Adds {@code documents.summary} for AI summaries on ingest (restored feature). */
public class V2__add_documents_summary_column extends BaseJavaMigration {

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
        if (!exists) {
            try (Statement st = conn.createStatement()) {
                st.execute("ALTER TABLE documents ADD COLUMN summary LONGTEXT NULL");
            }
        }
    }
}
