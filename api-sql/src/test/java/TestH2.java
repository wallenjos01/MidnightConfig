import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.mdcfg.sql.*;

public class TestH2 {

    @Test
    public void testH2() {

        SQLConnection conn = Common.getDBType("h2").create("h2/test");
        Common.testAll(conn);
        conn.close();
    }

    @Test
    public void testH2Prefixed() {

        SQLConnection conn = Common.getDBType("h2").create("h2/test", null, null, "pre_", new ConfigSection());
        Common.testAll(conn);
        conn.close();
    }

    @Test
    public void testPreset() {

        PresetRegistry reg = PresetRegistry.SERIALIZER.deserialize(ConfigContext.INSTANCE, new ConfigSection()
                .with("repository", new ConfigSection()
                        .with("type", "maven")
                        .with("folder", "drivers")
                )
                .with("presets", new ConfigSection()
                        .with("default", new DatabasePreset("h2", "h2/test", null, null, null, null, new ConfigSection()), DatabasePreset.SERIALIZER)
                )
        ).getOrThrow();

        try {
            reg.connect(new ConfigSection().with("preset", "default")).thenAccept(Common::testAll).get();
            reg.connect(new ConfigSection().with("preset", "default").with("table_prefix", "pre_")).thenAccept(Common::testAll).get();
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    private static class TableObject {
        private final int id;
        private final String name;

        public TableObject(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public static final Serializer<TableObject> SERIALIZER = ObjectSerializer.create(
                Serializer.INT.entry("id", to -> to.id),
                Serializer.STRING.entry("name", to -> to.name),
                TableObject::new
        );
    }

    @Test
    public void testWrongCase() {

        SQLConnection conn = Common.getDBType("h2").create("h2/test");

        TableSchema ts = TableSchema.builder()
            .withColumn("id", DataType.INTEGER)
            .withColumn("name", DataType.VARCHAR)
            .build();

        if(conn.hasTable("wrong_case")) {
            conn.dropTable("wrong_case").execute();
        }

        conn.createTable("wrong_case", ts).execute();

        conn.insert("wrong_case", ts).addRow(
                TableObject.SERIALIZER.serialize(ConfigContext.INSTANCE, new TableObject(42, "Test")).getOrThrow().asSection()
        ).execute();

        ConfigSection row = conn.select("wrong_case").execute().get(0).toConfigSection();

        Assertions.assertEquals(42, row.getInt("id"));
        Assertions.assertEquals("Test", row.getString("name"));

        TableObject obj = TableObject.SERIALIZER.deserialize(ConfigContext.INSTANCE, row).getOrThrow();

        Assertions.assertEquals(42, obj.id);
        Assertions.assertEquals("Test", obj.name);

        conn.close();
    }
}
