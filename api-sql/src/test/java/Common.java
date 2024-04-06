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

        conn.insert("test_where", schema, new ConfigSection().with("id", 1).with("name", "Test User 1"));
        conn.insert("test_where", schema, new ConfigSection().with("id", 2).with("name", "Test User 2"));
        conn.insert("test_where", schema, new ConfigSection().with("id", 3).with("name", "Test User 3"));

        // EQUALS
        List<ConfigSection> results = conn.select("test_where", schema, Where.equals("id", SQLDataValue.of(2)));
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(2, results.get(0).getByte("id"));
        Assertions.assertEquals("Test User 2", results.get(0).getString("name"));

        // GREATER_THAN
        results = conn.select("test_where", schema, Where.greaterThan("id", SQLDataValue.of(2)));
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(3, results.get(0).getByte("id"));
        Assertions.assertEquals("Test User 3", results.get(0).getString("name"));

        // LESS_THAN
        results = conn.select("test_where", schema, Where.lessThan("id", SQLDataValue.of(3)));
        Assertions.assertEquals(2, results.size());
        Assertions.assertEquals(1, results.get(0).getByte("id"));
        Assertions.assertEquals("Test User 1", results.get(0).getString("name"));
        Assertions.assertEquals(2, results.get(1).getByte("id"));
        Assertions.assertEquals("Test User 2", results.get(1).getString("name"));

        // AT_LEAST
        results = conn.select("test_where", schema, Where.atLeast("id", SQLDataValue.of(2)));
        Assertions.assertEquals(2, results.size());
        Assertions.assertEquals(2, results.get(0).getByte("id"));
        Assertions.assertEquals("Test User 2", results.get(0).getString("name"));
        Assertions.assertEquals(3, results.get(1).getByte("id"));
        Assertions.assertEquals("Test User 3", results.get(1).getString("name"));

        // AT_MOST
        results = conn.select("test_where", schema, Where.atMost("id", SQLDataValue.of(3)));
        Assertions.assertEquals(3, results.size());
        Assertions.assertEquals(1, results.get(0).getByte("id"));
        Assertions.assertEquals("Test User 1", results.get(0).getString("name"));
        Assertions.assertEquals(2, results.get(1).getByte("id"));
        Assertions.assertEquals("Test User 2", results.get(1).getString("name"));;
        Assertions.assertEquals(3, results.get(2).getByte("id"));
        Assertions.assertEquals("Test User 3", results.get(2).getString("name"));

        // BETWEEN
        results = conn.select("test_where", schema, Where.between("id", SQLDataValue.of(1), SQLDataValue.of(2)));
        Assertions.assertEquals(2, results.size());
        Assertions.assertEquals(1, results.get(0).getByte("id"));
        Assertions.assertEquals("Test User 1", results.get(0).getString("name"));
        Assertions.assertEquals(2, results.get(1).getByte("id"));
        Assertions.assertEquals("Test User 2", results.get(1).getString("name"));


        // AND
        results = conn.select("test_where", schema, Where.equals("id", SQLDataValue.of(3)).and(Where.equals("name", SQLDataValue.of("Test User 3"))));
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(3, results.get(0).getByte("id"));
        Assertions.assertEquals("Test User 3", results.get(0).getString("name"));

        // OR
        results = conn.select("test_where", schema, Where.equals("id", SQLDataValue.of(1)).or(Where.equals("id", SQLDataValue.of(3))));
        Assertions.assertEquals(2, results.size());
        Assertions.assertEquals(1, results.get(0).getByte("id"));
        Assertions.assertEquals("Test User 1", results.get(0).getString("name"));
        Assertions.assertEquals(3, results.get(1).getByte("id"));
        Assertions.assertEquals("Test User 3", results.get(1).getString("name"));

    }

}
