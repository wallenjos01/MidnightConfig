import org.junit.jupiter.api.Assertions;
import org.wallentines.mdcfg.ConfigBlob;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.sql.*;
import org.wallentines.mdcfg.sql.stmt.Expression;
import org.wallentines.mdcfg.sql.stmt.Select;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Arrays;

public class Common {

    private static final DriverRepository REPOSITORY = new DriverRepository.Maven(Paths.get("drivers"));

    public static DatabaseType getDBType(String name) {

        return REPOSITORY.getDriver(name);
    }

    public static void testAll(SQLConnection conn) {
        testBasics(conn);
        testUnique(conn);
        testNumberTypes(conn);
        testStringTypes(conn);
        testBlob(conn);
        testNull(conn);
        testWhere(conn);
        testJoins(conn);
        testOrderBy(conn);
        testAggregate(conn);
        testGroupBy(conn);
        testCascade(conn);
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
        conn.insert("test", schema).addRow(new ConfigSection().with("name", "Test User")).execute();

        QueryResult results = conn.select("test").execute();

        Assertions.assertEquals(1, results.rows());
        Assertions.assertEquals(2, results.get(0).columns());
        Assertions.assertEquals(1, results.get(0).get("id").getValue());
        Assertions.assertEquals("Test User", results.get(0).get("name").getValue());

        conn.delete("test").execute();
        conn.dropTable("test").execute();
    }

    public static void testUnique(SQLConnection conn) {
        TableSchema schema = TableSchema.builder()
                .withColumn("id", DataType.INTEGER)
                .withColumn("name", DataType.VARCHAR(255))
                .withTableConstraint(TableConstraint.UNIQUE(Arrays.asList("id", "name")))
                .build();

        if(conn.hasTable("test_unique")) {
            conn.dropTable("test_unique").execute();
        }

        conn.createTable("test_unique", schema).execute();
        conn.insert("test_unique", new ConfigSection().with("id", 1).with("name", "Test User")).execute();

        QueryResult results = conn.select("test_unique").execute();

        Assertions.assertEquals(1, results.rows());
        Assertions.assertEquals(2, results.get(0).columns());
        Assertions.assertEquals(1, results.get(0).get("id").getValue());
        Assertions.assertEquals("Test User", results.get(0).get("name").getValue());

        conn.delete("test_unique").execute();
        conn.dropTable("test_unique").execute();
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
        Assertions.assertEquals(100.0f, ((Number) results.get(0).get("_float").getValue()).floatValue());
        Assertions.assertEquals(300000.0, results.get(0).get("_double").getValue());
        Assertions.assertEquals(10.12575, ((BigDecimal) results.get(0).get("_decimal").getValue()).doubleValue());

        conn.delete("test_nums").execute();
        conn.dropTable("test_nums").execute();
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

        conn.delete("test_strings").execute();
        conn.dropTable("test_strings").execute();

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

        conn.delete("test_blobs").execute();
        conn.dropTable("test_blobs").execute();
    }

    public static void testNull(SQLConnection conn) {
        TableSchema schema = TableSchema.builder()
                .withColumn("_nullable", DataType.VARCHAR(255))
                .build();

        if(conn.hasTable("test_null")) {
            conn.dropTable("test_null").execute();
        }

        conn.createTable("test_null", schema).execute();

        ConfigSection data = new ConfigSection();
        conn.insert("test_null", schema).addRow(data).execute();

        QueryResult results = conn.select("test_null").execute();

        Assertions.assertEquals(1, results.rows());
        Assertions.assertEquals(1, results.get(0).columns());
        Assertions.assertNull(results.get(0).getValue("_nullable"));

        conn.delete("test_null").execute();
        conn.dropTable("test_null").execute();
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
        Assertions.assertEquals("Test User 2", results.get(1).getValue("name"));
        Assertions.assertEquals((byte) 3, results.get(2).getValue("id"));
        Assertions.assertEquals("Test User 3", results.get(2).getValue("name"));

        // BETWEEN
        results = conn.select("test_where")
                .where(Expression.between("id", DataType.INTEGER.create(1), DataType.INTEGER.create(2)))
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

        conn.delete("test_where").where(Condition.equals("id", DataType.INTEGER.create(2))).execute();
        results = conn.select("test_where").execute();
        Assertions.assertEquals(2, results.rows());
        Assertions.assertEquals((byte) 1, results.get(0).getValue("id"));
        Assertions.assertEquals("Test User 1", results.get(0).getValue("name"));
        Assertions.assertEquals((byte) 3, results.get(1).getValue("id"));
        Assertions.assertEquals("Test User 3", results.get(1).getValue("name"));

        conn.delete("test_where").execute();
        conn.dropTable("test_where").execute();

    }

    public static void testJoins(SQLConnection conn) {

        TableSchema aSchema = TableSchema.builder()
                .withColumn(Column.builder("aId", DataType.TINYINT).withConstraint(Constraint.PRIMARY_KEY))
                .withColumn("aName", DataType.VARCHAR(255))
                .build();

        TableSchema bSchema = TableSchema.builder()
                .withColumn("bId", DataType.TINYINT)
                .withColumn("bName", DataType.VARCHAR(255))
                .withColumn(Column.builder("aId", DataType.TINYINT).withConstraint(Constraint.FOREIGN_KEY(new ColumnRef("aTable", "aId"))))
                .withTableConstraint(TableConstraint.PRIMARY_KEY("bId"))
                .build();

        TableSchema cSchema = TableSchema.builder()
                .withColumn("cId", DataType.TINYINT)
                .withColumn("cName", DataType.VARCHAR(255))
                .withColumn("bId", DataType.TINYINT)
                .withTableConstraint(TableConstraint.FOREIGN_KEY("bId", new ColumnRef("bTable", "bId")))
                .build();

        if(conn.hasTable("bTable")) {
            conn.dropTable("bTable").execute();
        }
        if(conn.hasTable("aTable")) {
            conn.dropTable("aTable").execute();
        }
        if(conn.hasTable("cTable")) {
            conn.dropTable("cTable").execute();
        }

        conn.createTable("aTable", aSchema).execute();
        conn.createTable("bTable", bSchema).execute();
        conn.createTable("cTable", cSchema).execute();

        conn.insert("aTable", aSchema).addRow(new ConfigSection().with("aId", 1).with("aName", "A1")).execute();
        conn.insert("aTable", aSchema).addRow(new ConfigSection().with("aId", 2).with("aName", "A2")).execute();

        conn.insert("bTable", bSchema).addRow(new ConfigSection().with("bId", 1).with("bName", "B1").with("aId", 1)).execute();
        conn.insert("bTable", bSchema).addRow(new ConfigSection().with("bId", 2).with("bName", "B2").with("aId", 2)).execute();

        conn.insert("cTable", cSchema).addRow(new ConfigSection().with("cId", 1).with("cName", "C1").with("bId", 1)).execute();
        conn.insert("cTable", cSchema).addRow(new ConfigSection().with("cId", 2).with("cName", "C2").with("bId", 2)).execute();

        QueryResult res = conn.select("bTable")
                .join(Select.JoinType.INNER, "aTable", "aId")
                .execute();

        Assertions.assertEquals(2, res.rows());
        Assertions.assertEquals((byte) 1, res.get(0).getValue("bId"));
        Assertions.assertEquals("B1", res.get(0).getValue("bName"));
        Assertions.assertEquals("A1", res.get(0).getValue("aName"));
        Assertions.assertEquals((byte) 2, res.get(1).getValue("bId"));
        Assertions.assertEquals("B2", res.get(1).getValue("bName"));
        Assertions.assertEquals("A2", res.get(1).getValue("aName"));

        res = conn.select("bTable")
                .where(
                        Expression.exists(conn.select("aTable")
                                .where(Condition.equals(
                                        conn.column("aTable", "aId"),
                                        conn.column("bTable", "aId")
                                ))
                        ))
                .execute();

        Assertions.assertEquals(2, res.rows());
        Assertions.assertEquals((byte) 1, res.get(0).getValue("bId"));
        Assertions.assertEquals("B1", res.get(0).getValue("bName"));
        Assertions.assertEquals((byte) 2, res.get(1).getValue("bId"));
        Assertions.assertEquals("B2", res.get(1).getValue("bName"));

        conn.delete("cTable").execute();
        conn.dropTable("cTable").execute();

        conn.delete("bTable").execute();
        conn.dropTable("bTable").execute();

        conn.delete("aTable").execute();
        conn.dropTable("aTable").execute();
    }


    public static void testOrderBy(SQLConnection conn) {
        TableSchema schema = TableSchema.builder()
                .withColumn("id", DataType.TINYINT)
                .withColumn("name", DataType.VARCHAR(255))
                .build();

        if(conn.hasTable("test_order")) {
            conn.dropTable("test_order").execute();
        }


        conn.createTable("test_order", schema).execute();

        conn.insert("test_order", schema)
                .addRow(new ConfigSection().with("id", 1).with("name", "Banana"))
                .addRow(new ConfigSection().with("id", 2).with("name", "Apple"))
                .addRow(new ConfigSection().with("id", 3).with("name", "Strawberry"))
                .execute();

        QueryResult res = conn.select("test_order")
                .orderBy("name")
                .execute();

        Assertions.assertEquals(3, res.rows());
        Assertions.assertEquals("Apple", res.get(0).get("name", DataType.VARCHAR).getValue());
        Assertions.assertEquals("Banana", res.get(1).get("name", DataType.VARCHAR).getValue());
        Assertions.assertEquals("Strawberry", res.get(2).get("name", DataType.VARCHAR).getValue());

        res = conn.select("test_order")
                .orderBy(Select.SortOrder.DESCENDING, "name")
                .execute();

        Assertions.assertEquals(3, res.rows());
        Assertions.assertEquals("Apple", res.get(2).get("name", DataType.VARCHAR).getValue());
        Assertions.assertEquals("Banana", res.get(1).get("name", DataType.VARCHAR).getValue());
        Assertions.assertEquals("Strawberry", res.get(0).get("name", DataType.VARCHAR).getValue());

        conn.delete("test_order").execute();
        conn.dropTable("test_order").execute();
    }

    public static void testAggregate(SQLConnection conn) {

        TableSchema schema = TableSchema.builder()
                .withColumn("id", DataType.TINYINT)
                .withColumn("val", DataType.INTEGER)
                .build();

        if(conn.hasTable("test_aggregate")) {
            conn.dropTable("test_aggregate").execute();
        }


        conn.createTable("test_aggregate", schema).execute();

        conn.insert("test_aggregate", schema)
                .addRow(new ConfigSection().with("id", 1).with("val", 5))
                .addRow(new ConfigSection().with("id", 2).with("val", 10))
                .addRow(new ConfigSection().with("id", 3).with("val", 15))
                .addRow(new ConfigSection().with("id", 4).with("val", 20))
                .addRow(new ConfigSection().with("id", 5).with("val", 25))
                .execute();

        QueryResult res = conn.select("test_aggregate")
                .withColumn(Expression.max("val"))
                .withColumn(Expression.min("val"))
                .withColumn(Expression.sum("val"))
                .withColumn(Expression.average("val"))
                .execute();


        Assertions.assertEquals(1, res.rows());
        Assertions.assertEquals(25, res.get(0).getInt(0));
        Assertions.assertEquals(5, res.get(0).getInt(1));
        Assertions.assertEquals(75, res.get(0).getInt(2));
        Assertions.assertEquals(15, res.get(0).getInt(3));

        conn.delete("test_aggregate").execute();
        conn.dropTable("test_aggregate").execute();
    }


    public static void testGroupBy(SQLConnection conn) {
        TableSchema schema = TableSchema.builder()
                .withColumn("id", DataType.TINYINT)
                .withColumn("category", DataType.VARCHAR(255))
                .build();

        if(conn.hasTable("test_group")) {
            conn.dropTable("test_group").execute();
        }


        conn.createTable("test_group", schema).execute();

        conn.insert("test_group", schema)
                .addRow(new ConfigSection().with("id", 1).with("category", "CAT1"))
                .addRow(new ConfigSection().with("id", 2).with("category", "CAT2"))
                .addRow(new ConfigSection().with("id", 3).with("category", "CAT1"))
                .execute();

        QueryResult res = conn.select("test_group")
                .withColumn(Expression.count("category"))
                .groupBy("category")
                .execute();

        Assertions.assertEquals(2, res.rows());

        long total = res.get(0).getInt(0) + res.get(1).getInt(0);
        Assertions.assertEquals(3, total);

        conn.delete("test_group").execute();
        conn.dropTable("test_group").execute();
    }

    public static void testCascade(SQLConnection conn) {

        TableSchema aSchema = TableSchema.builder()
                .withColumn(Column.builder("aId", DataType.TINYINT).withConstraint(Constraint.PRIMARY_KEY))
                .withColumn("aName", DataType.VARCHAR(255))
                .build();

        TableSchema bSchema = TableSchema.builder()
                .withColumn("bId", DataType.TINYINT)
                .withColumn("bName", DataType.VARCHAR(255))
                .withColumn(Column.builder("aId", DataType.TINYINT))
                .withTableConstraint(TableConstraint.PRIMARY_KEY("bId"))
                .withTableConstraint(TableConstraint.FOREIGN_KEY("aId", new ColumnRef("aTable", "aId")).cascade())
                .build();

        if(conn.hasTable("bTable")) {
            conn.dropTable("bTable").execute();
        }
        if(conn.hasTable("aTable")) {
            conn.dropTable("aTable").execute();
        }

        conn.createTable("aTable", aSchema).execute();
        conn.createTable("bTable", bSchema).execute();

        conn.insert("aTable", aSchema).addRow(new ConfigSection().with("aId", 1).with("aName", "A1")).execute();
        conn.insert("aTable", aSchema).addRow(new ConfigSection().with("aId", 2).with("aName", "A2")).execute();

        conn.insert("bTable", bSchema).addRow(new ConfigSection().with("bId", 1).with("bName", "B1").with("aId", 1)).execute();
        conn.insert("bTable", bSchema).addRow(new ConfigSection().with("bId", 2).with("bName", "B2").with("aId", 2)).execute();

        QueryResult res = conn.select("bTable")
                .join(Select.JoinType.INNER, "aTable", "aId")
                .execute();

        Assertions.assertEquals(2, res.rows());

        conn.delete("aTable").execute();

        res = conn.select("bTable")
                .join(Select.JoinType.INNER, "aTable", "aId")
                .execute();
        Assertions.assertEquals(0, res.rows());

        conn.dropTable("bTable").execute();
        conn.dropTable("aTable").execute();
    }

}
