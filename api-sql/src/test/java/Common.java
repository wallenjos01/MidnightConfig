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

    public static void testNumberTypes(SQLConnection conn) {
        TableSchema schema = TableSchema.builder()
                .withColumn("_bool", ColumnType.BOOL)
                .withColumn("_tinyint", ColumnType.TINYINT)
                .withColumn("_smallint", ColumnType.SMALLINT)
                .withColumn("_mediumint", ColumnType.MEDIUMINT)
                .withColumn("_int", ColumnType.INT)
                .withColumn("_bigint", ColumnType.BIGINT)
                .withColumn("_float", ColumnType.FLOAT(24))
                .withColumn("_double", ColumnType.FLOAT(53))
                .withColumn("_decimal", ColumnType.DECIMAL(10,5))
                .build();

        if(conn.hasTable("test_nums")) {
            conn.dropTable("test_nums");
        }

        conn.createTable("test_nums", schema);

        conn.insert("test_nums", schema, new ConfigSection()
                .with("_bool", true)
                .with("_tinyint", 9)
                .with("_smallint", 12374)
                .with("_mediumint", 8383307)
                .with("_int", 588865932)
                .with("_bigint", 12031984858L)
                .with("_float", 100.0f)
                .with("_double", 300000.0)
                .with("_decimal", 10.12575));

        List<ConfigSection> results = conn.select("test_nums", schema);

        Assertions.assertEquals(1, results.size());
        Assertions.assertTrue(results.get(0).getBoolean("_bool"));
        Assertions.assertEquals(9, results.get(0).getInt("_tinyint"));
        Assertions.assertEquals(12374, results.get(0).getInt("_smallint"));
        Assertions.assertEquals(8383307, results.get(0).getInt("_mediumint"));
        Assertions.assertEquals(588865932, results.get(0).getInt("_int"));
        Assertions.assertEquals(12031984858L, results.get(0).getLong("_bigint"));
        Assertions.assertEquals(100.0f, results.get(0).getFloat("_float"));
        Assertions.assertEquals(300000.0, results.get(0).getDouble("_double"));
        Assertions.assertEquals(10.12575, results.get(0).getDouble("_decimal"));

    }

    public static void testStringTypes(SQLConnection conn) {
        TableSchema schema = TableSchema.builder()
                .withColumn("_char", ColumnType.CHAR(255))
                .withColumn("_varchar", ColumnType.VARCHAR(999))
                .withColumn("_tinytext", ColumnType.TINYTEXT)
                .withColumn("_text", ColumnType.TEXT(3000))
                .withColumn("_mediumtext", ColumnType.MEDIUMTEXT(98342))
                .withColumn("_longtext", ColumnType.LONGTEXT(17000000))
                .build();

        if(conn.hasTable("test_strings")) {
            conn.dropTable("test_strings");
        }

        conn.createTable("test_strings", schema);

        conn.insert("test_strings", schema, new ConfigSection()
                .with("_char", "CHAR TYPE")
                .with("_varchar", "VARCHAR TYPE")
                .with("_tinytext", "TINYTEXT TYPE")
                .with("_text", "TEXT TYPE")
                .with("_mediumtext", "MEDIUMTEXT TYPE")
                .with("_longtext", "LONGTEXT TYPE"));

        List<ConfigSection> results = conn.select("test_strings", schema);

        Assertions.assertEquals(1, results.size());
        Assertions.assertTrue(results.get(0).getString("_char").startsWith("CHAR TYPE"));
        Assertions.assertEquals("VARCHAR TYPE", results.get(0).getString("_varchar"));
        Assertions.assertEquals("TINYTEXT TYPE", results.get(0).getString("_tinytext"));
        Assertions.assertEquals("TEXT TYPE", results.get(0).getString("_text"));
        Assertions.assertEquals("MEDIUMTEXT TYPE", results.get(0).getString("_mediumtext"));
        Assertions.assertEquals("LONGTEXT TYPE", results.get(0).getString("_longtext"));

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
