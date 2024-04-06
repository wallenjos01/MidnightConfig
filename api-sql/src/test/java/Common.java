import org.junit.jupiter.api.Assertions;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.sql.*;

import java.util.List;

public class Common {

    public static void testBasics(SQLConnection conn) {
        TableSchema schema = TableSchema.builder()
                .withColumn("id", ColumnType.SMALLINT)
                .withColumn("name", ColumnType.TINYTEXT)
                .build();

        if(conn.hasTable("test")) {
            conn.dropTable("test");
        }

        conn.createTable("test", schema);

        conn.insert("test", schema, new ConfigSection().with("id", 1).with("name", "Test User"));

        List<ConfigSection> results = conn.select("test", schema);

        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(1, results.get(0).getByte("id"));
        Assertions.assertEquals("Test User", results.get(0).getString("name"));
    }

    public static void testWhere(SQLConnection conn) {
        TableSchema schema = TableSchema.builder()
                .withColumn("id", ColumnType.SMALLINT)
                .withColumn("name", ColumnType.TINYTEXT)
                .build();

        if(conn.hasTable("test_where")) {
            conn.dropTable("test_where");
        }


        conn.createTable("test_where", schema);

        conn.insert("test", schema, new ConfigSection().with("id", 1).with("name", "Test User 1"));
        conn.insert("test", schema, new ConfigSection().with("id", 2).with("name", "Test User 2"));
        conn.insert("test", schema, new ConfigSection().with("id", 3).with("name", "Test User 3"));

        List<ConfigSection> results = conn.select("test", schema, Where.equals("id", SQLDataValue.of(2)));

        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(2, results.get(0).getByte("id"));
        Assertions.assertEquals("Test User 2", results.get(0).getString("name"));

    }

}
