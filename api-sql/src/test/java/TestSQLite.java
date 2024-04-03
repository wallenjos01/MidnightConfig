import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.sql.ColumnType;
import org.wallentines.mdcfg.sql.DatabaseType;
import org.wallentines.mdcfg.sql.SQLConnection;
import org.wallentines.mdcfg.sql.TableSchema;

import java.io.File;
import java.util.List;

public class TestSQLite {

    @Test
    public void testSQLite() {

        File db = new File("test.db");
        SQLConnection conn = DatabaseType.sqlite().create(db.getAbsolutePath());

        TableSchema schema = TableSchema.builder()
                .withColumn("id", ColumnType.SMALLINT)
                .withColumn("name", ColumnType.VARCHAR(256))
                .build();

        if(conn.getTables().contains("test")) {
            conn.clearTable("test");
        } else {
            Assertions.assertTrue(conn.createTable("test", schema));
        }

        conn.insert("test", schema, new ConfigSection().with("id", 1).with("name", "Test User"));
        conn.close();

        conn = DatabaseType.sqlite().create(db.getAbsolutePath());
        List<ConfigSection> results = conn.select("test", schema);

        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(1, results.get(0).getByte("id"));
        Assertions.assertEquals("Test User", results.get(0).getString("name"));

    }

}
