import org.junit.jupiter.api.Assertions;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.sql.ColumnType;
import org.wallentines.mdcfg.sql.DatabaseType;
import org.wallentines.mdcfg.sql.SQLConnection;
import org.wallentines.mdcfg.sql.TableSchema;

import java.util.List;

public class Common {

    public static void testBasics(SQLConnection conn) {
        TableSchema schema = TableSchema.builder()
                .withColumn("id", ColumnType.SMALLINT)
                .withColumn("name", ColumnType.TINYTEXT)
                .build();

        if(conn.getTables().contains("test")) {
            conn.dropTable("test");
        }

        conn.createTable("test", schema);

        conn.insert("test", schema, new ConfigSection().with("id", 1).with("name", "Test User"));

        List<ConfigSection> results = conn.select("test", schema);

        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(1, results.get(0).getByte("id"));
        Assertions.assertEquals("Test User", results.get(0).getString("name"));
        conn.close();
    }

}
