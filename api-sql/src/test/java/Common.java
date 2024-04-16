import org.junit.jupiter.api.Assertions;
import org.wallentines.mdcfg.ConfigBlob;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.sql.*;

import java.math.BigDecimal;

public class Common {

    private static final DriverRepository REPOSITORY = new DriverRepository.Classpath();

    public static DatabaseType getDBType(String name) {

        return REPOSITORY.getDriver(name);
    }

    public static void testBasics(SQLConnection conn) {
        TableSchema schema = TableSchema.builder()
                .withColumn(Column.builder("id", DataType.INTEGER).withConstraint(Constraint.PRIMARY_KEY).withConstraint(Constraint.AUTO_INCREMENT))
                .withColumn("name", DataType.VARCHAR(255))
                .build();

        if(conn.hasTable("test")) {
            conn.dropTable("test").execute();
        }

        conn.createTable("test", schema).execute();
        conn.insert("test", schema)
                .addRow(new ConfigSection()
                        .with("name", "Test User"))
                .execute();

        QueryResult results = conn.select("test").execute();

        Assertions.assertEquals(1, results.rows());
        Assertions.assertEquals(2, results.get(0).columns());
        Assertions.assertEquals(1, results.get(0).get("id").getValue());
        Assertions.assertEquals("Test User", results.get(0).get("name").getValue());
    }

    public static void testNumberTypes(SQLConnection conn) {
        TableSchema schema = TableSchema.builder()
                .withColumn("_bit", DataType.BIT)
                .withColumn("_bool", DataType.BOOLEAN)
                .withColumn("_tinyint", DataType.TINYINT)
                .withColumn("_smallint", DataType.SMALLINT)
                .withColumn("_int", DataType.INTEGER)
                .withColumn("_bigint", DataType.BIGINT)
                .withColumn("_float", DataType.REAL)
                .withColumn("_double", DataType.DOUBLE)
                .withColumn("_decimal", DataType.DECIMAL(10,5))
                .build();

        if(conn.hasTable("test_nums")) {
            conn.dropTable("test_nums").execute();
        }

        conn.createTable("test_nums", schema).execute();
        conn.insert("test_nums", schema)
                .addRow(new ConfigSection()
                        .with("_bit", true)
                        .with("_bool", true)
                        .with("_tinyint", 9)
                        .with("_smallint", 12374)
                        .with("_int", 588865932)
                        .with("_bigint", 12031984858L)
                        .with("_float", 100.0f)
                        .with("_double", 300000.0)
                        .with("_decimal", 10.12575))
                .execute();

        QueryResult results = conn.select("test_nums").execute();

        Assertions.assertEquals(1, results.rows());
        Assertions.assertEquals(9, results.get(0).columns());
        Assertions.assertNotEquals(0, results.get(0).getValue("_bit"));
        Assertions.assertNotEquals(0, results.get(0).getValue("_bool"));
        Assertions.assertEquals((byte) 9, results.get(0).get("_tinyint").getValue());
        Assertions.assertEquals((short) 12374, results.get(0).get("_smallint").getValue());
        Assertions.assertEquals(588865932, results.get(0).get("_int").getValue());
        Assertions.assertEquals(12031984858L, results.get(0).get("_bigint").getValue());
        Assertions.assertEquals(100.0f, results.get(0).get("_float").getValue());
        Assertions.assertEquals(300000.0, results.get(0).get("_double").getValue());
        Assertions.assertEquals(10.12575, ((BigDecimal) results.get(0).get("_decimal").getValue()).doubleValue());

    }

    public static void testStringTypes(SQLConnection conn) {
        TableSchema schema = TableSchema.builder()
                .withColumn("_char", DataType.CHAR(255))
                .withColumn("_varchar", DataType.VARCHAR(999))
                .withColumn("_longvarchar", DataType.LONGVARCHAR)
                .build();

        if(conn.hasTable("test_strings")) {
            conn.dropTable("test_strings").execute();
        }

        conn.createTable("test_strings", schema).execute();

        conn.insert("test_strings", schema)
                .addRow(new ConfigSection()
                        .with("_char", "CHAR TYPE")
                        .with("_varchar", "VARCHAR TYPE")
                        .with("_longvarchar", "LONGVARCHAR TYPE"))
                .execute();

        QueryResult results = conn.select("test_strings").execute();

        Assertions.assertEquals(1, results.rows());

        ConfigSection row = results.get(0).toConfigSection();

        Assertions.assertTrue(row.getString(conn.fixIdentifier("_char")).startsWith("CHAR TYPE"));
        Assertions.assertEquals("VARCHAR TYPE", row.getString(conn.fixIdentifier("_varchar")));
        Assertions.assertEquals("LONGVARCHAR TYPE", row.getString(conn.fixIdentifier("_longvarchar")));

    }

    public static void testBlob(SQLConnection conn) {
        TableSchema schema = TableSchema.builder()
                .withColumn("_blob", DataType.BLOB(3000))
                .build();

        if(conn.hasTable("test_blobs")) {
            conn.dropTable("test_blobs").execute();
        }

        conn.createTable("test_blobs", schema).execute();

        ConfigSection data = new ConfigSection()
                .with("_blob", new ConfigBlob("BLOB".getBytes()));

        conn.insert("test_blobs", schema).addRow(data).execute();

        QueryResult results = conn.select("test_blobs").execute();

        Assertions.assertEquals(1, results.rows());
        Assertions.assertEquals(1, results.get(0).columns());
        Assertions.assertEquals(data.get("_blob").asBlob().getData(), results.get(0).getValue("_blob"));

    }


    public static void testWhere(SQLConnection conn) {
        TableSchema schema = TableSchema.builder()
                .withColumn("id", DataType.TINYINT)
                .withColumn("name", DataType.VARCHAR(255))
                .build();

        if(conn.hasTable("test_where")) {
            conn.dropTable("test_where").execute();
        }


        conn.createTable("test_where", schema).execute();

        conn.insert("test_where", schema)
                .addRow(new ConfigSection().with("id", 1).with("name", "Test User 1"))
                .addRow(new ConfigSection().with("id", 2).with("name", "Test User 2"))
                .addRow(new ConfigSection().with("id", 3).with("name", "Test User 3"))
                .execute();

        // EQUALS
        QueryResult results = conn.select("test_where")
                .where(Condition.equals("id", DataType.INTEGER.create(2)))
                .execute();
        
        Assertions.assertEquals(1, results.rows());
        Assertions.assertEquals((byte) 2, results.get(0).getValue("id"));
        Assertions.assertEquals("Test User 2", results.get(0).getValue("name"));

        // GREATER_THAN
        results = conn.select("test_where")
                .where(Condition.greaterThan("id", DataType.INTEGER.create(2)))
                .execute();
        Assertions.assertEquals(1, results.rows());
        Assertions.assertEquals((byte) 3, results.get(0).getValue("id"));
        Assertions.assertEquals("Test User 3", results.get(0).getValue("name"));

        // LESS_THAN
        results = conn.select("test_where")
                .where(Condition.lessThan("id", DataType.INTEGER.create(3)))
                .execute();
        Assertions.assertEquals(2, results.rows());
        Assertions.assertEquals((byte) 1, results.get(0).getValue("id"));
        Assertions.assertEquals("Test User 1", results.get(0).getValue("name"));
        Assertions.assertEquals((byte) 2, results.get(1).getValue("id"));
        Assertions.assertEquals("Test User 2", results.get(1).getValue("name"));

        // AT_LEAST
        results = conn.select("test_where")
                .where(Condition.atLeast("id", DataType.INTEGER.create(2)))
                .execute();
        Assertions.assertEquals(2, results.rows());
        Assertions.assertEquals((byte) 2, results.get(0).getValue("id"));
        Assertions.assertEquals("Test User 2", results.get(0).getValue("name"));
        Assertions.assertEquals((byte) 3, results.get(1).getValue("id"));
        Assertions.assertEquals("Test User 3", results.get(1).getValue("name"));

        // AT_MOST
        results = conn.select("test_where")
                .where(Condition.atMost("id", DataType.INTEGER.create(3)))
                .execute();
        Assertions.assertEquals(3, results.rows());
        Assertions.assertEquals((byte) 1, results.get(0).getValue("id"));
        Assertions.assertEquals("Test User 1", results.get(0).getValue("name"));
        Assertions.assertEquals((byte) 2, results.get(1).getValue("id"));
        Assertions.assertEquals("Test User 2", results.get(1).getValue("name"));;
        Assertions.assertEquals((byte) 3, results.get(2).getValue("id"));
        Assertions.assertEquals("Test User 3", results.get(2).getValue("name"));

        // BETWEEN
        results = conn.select("test_where")
                .where(Condition.between("id", DataType.INTEGER.create(1), DataType.INTEGER.create(2)))
                .execute();
        Assertions.assertEquals(2, results.rows());
        Assertions.assertEquals((byte) 1, results.get(0).getValue("id"));
        Assertions.assertEquals("Test User 1", results.get(0).getValue("name"));
        Assertions.assertEquals((byte) 2, results.get(1).getValue("id"));
        Assertions.assertEquals("Test User 2", results.get(1).getValue("name"));


        // AND
        results = conn.select("test_where")
                .where(Condition.equals("id", DataType.INTEGER.create(3)).and(Condition.equals("name", DataType.VARCHAR.create("Test User 3"))))
                .execute();
        Assertions.assertEquals(1, results.rows());
        Assertions.assertEquals((byte) 3, results.get(0).getValue("id"));
        Assertions.assertEquals("Test User 3", results.get(0).getValue("name"));

        // OR
        results = conn.select("test_where")
                .where(Condition.equals("id", DataType.INTEGER.create(1)).or(Condition.equals("id", DataType.INTEGER.create(3))))
                .execute();
        Assertions.assertEquals(2, results.rows());
        Assertions.assertEquals((byte) 1, results.get(0).getValue("id"));
        Assertions.assertEquals("Test User 1", results.get(0).getValue("name"));
        Assertions.assertEquals((byte) 3, results.get(1).getValue("id"));
        Assertions.assertEquals("Test User 3", results.get(1).getValue("name"));

    }

}
