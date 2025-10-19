package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;

public class DatabasePreset {

    private final String driver;
    private final String url;
    private final String username;
    private final String password;
    private final String database;
    private final String tablePrefix;
    private final ConfigSection parameters;

    public String driver() { return driver; }
    public String url() {
        String baseUrl = url;
        String db = database();
        if (db != null) {
            baseUrl += "/" + db;
        }
        return baseUrl;
    }
    public String username() { return username; }
    public String password() { return password; }
    public String database() { return database; }
    public String tablePrefix() { return tablePrefix; }
    public ConfigSection parameters() { return parameters; }

    public String baseUrl() { return url; }

    public DatabasePreset(String driver, String url, String username,
                          String password, String database, String tablePrefix,
                          ConfigSection parameters) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
        this.database = database;
        this.tablePrefix = tablePrefix;
        this.parameters = parameters;
    }

    public static final Serializer<DatabasePreset> SERIALIZER =
        ObjectSerializer.create(
            Serializer.STRING.orEnv().entry("driver", dp -> dp.driver),
            Serializer.STRING.orEnv().entry("url", dp -> dp.url),
            Serializer.STRING.orEnv()
                .<DatabasePreset>entry("username", dp -> dp.username)
                .optional(),
            Serializer.STRING.orEnv()
                .<DatabasePreset>entry("password", dp -> dp.password)
                .optional(),
            Serializer.STRING.orEnv()
                .<DatabasePreset>entry("database", dp -> dp.database)
                .optional(),
            Serializer.STRING.orEnv()
                .<DatabasePreset>entry("table_prefix", dp -> dp.tablePrefix)
                .optional(),
            ConfigSection.SERIALIZER
                .entry("parameters", DatabasePreset::parameters)
                .orElse(new ConfigSection()),
            DatabasePreset::new);

    public DatabasePreset finalize(ConfigSection config) {

        ConfigSection merged = config.copy();
        merged.fill(SERIALIZER.serialize(ConfigContext.INSTANCE, this)
                        .getOrThrow()
                        .asSection());

        SerializeResult<DatabasePreset> decoded =
            SERIALIZER.deserialize(ConfigContext.INSTANCE, merged);
        if (!decoded.isComplete()) {
            return this;
        }

        return decoded.getOrNull();
    }
}
