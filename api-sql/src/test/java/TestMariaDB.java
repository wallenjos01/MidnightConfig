import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.sql.DatabaseType;
import org.wallentines.mdcfg.sql.PresetRegistry;
import org.wallentines.mdcfg.sql.SQLConnection;

public class TestMariaDB {

    @Test
    public void testMySQL() {

        String url = System.getenv("TEST_MARIADB_URL");
        String user = System.getenv("TEST_MARIADB_USER");
        String pass = System.getenv("TEST_MARIADB_PASS");

        if (url != null && user != null && pass != null) {

            DatabaseType type = Common.getDBType("mariadb");
            try (SQLConnection conn = type.create(url, user, pass)) {
                Common.testAll(conn);
            }
        }
    }

    @Test
    public void testPreset() {

        String url = System.getenv("TEST_MARIADB_URL");
        if (url == null)
            return;

        PresetRegistry reg =
            PresetRegistry.SERIALIZER
                .deserialize(
                    ConfigContext.INSTANCE,
                    new ConfigSection()
                        .with("repository", new ConfigSection()
                                                .with("type", "maven")
                                                .with("folder", "drivers"))
                        .with(
                            "presets",
                            new ConfigSection().with(
                                "default",
                                new ConfigSection()
                                    .with("driver", "mariadb")
                                    .with("url", new ConfigSection().with(
                                                     "env", "TEST_MARIADB_URL"))
                                    .with("username",
                                          new ConfigSection().with(
                                              "env", "TEST_MARIADB_USER"))
                                    .with("password",
                                          new ConfigSection().with(
                                              "env", "TEST_MARIADB_PASS")))))
                .getOrThrow();

        try {
            reg.connect(new ConfigSection().with("preset", "default"))
                .thenAccept(Common::testAll)
                .get();
            reg.connect(new ConfigSection()
                            .with("preset", "default")
                            .with("table_prefix", "pre_"))
                .thenAccept(Common::testAll)
                .get();
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }
}
