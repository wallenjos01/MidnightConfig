package org.wallentines.mdcfg.sql;

import java.util.Optional;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;

public class DatabasePreset {

    private final PresetParameter driver;
    private final PresetParameter url;
    private final PresetParameter username;
    private final PresetParameter password;
    private final PresetParameter database;
    private final PresetParameter tablePrefix;
    private final ConfigSection parameters;

    public String driver() { return driver.get(); }
    public String url() {
        String baseUrl = url.get();
        String db = database();
        if (db != null) {
            baseUrl += "/" + db;
        }
        return baseUrl;
    }
    public String username() { return username.get(); }
    public String password() { return password.get(); }
    public String database() { return database.get(); }
    public String tablePrefix() { return tablePrefix.get(); }
    public ConfigSection parameters() { return parameters; }

    public String baseUrl() { return url.get(); }

    public DatabasePreset(PresetParameter driver, PresetParameter url,
                          PresetParameter username, PresetParameter password,
                          PresetParameter database, PresetParameter tablePrefix,
                          ConfigSection parameters) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
        this.database = database;
        this.tablePrefix = tablePrefix;
        this.parameters = parameters;
    }

    public DatabasePreset(String driver, String url, String username,
                          String password, String database, String tablePrefix,
                          ConfigSection parameters) {
        this(new PresetParameter(driver), new PresetParameter(url),
             new PresetParameter(username), new PresetParameter(password),
             new PresetParameter(database), new PresetParameter(tablePrefix),
             parameters);
    }

    public static class PresetParameter {

        public final String value;
        public final String env;

        public PresetParameter(String value, String env) {
            this.value = value == null ? "" : value;
            this.env = env;
        }

        public PresetParameter(String value) { this(value, null); }

        public String get() {
            if (value != null)
                return value;
            if (env != null)
                return System.getenv(env);
            return "";
        }

        public static final PresetParameter EMPTY =
            new PresetParameter("", null);

        public static final Serializer<PresetParameter> SERIALIZER =
            Serializer.STRING
                .<PresetParameter>flatMap(
                    p -> p.value, str -> new PresetParameter(str, null))
                .or(Serializer.STRING.fieldOf("env").flatMap(
                    p -> p.env, str -> new PresetParameter(null, str)));
    }

    public static final Serializer<DatabasePreset> SERIALIZER =
        ObjectSerializer.create(
            PresetParameter.SERIALIZER.entry("driver", dp -> dp.driver),
            PresetParameter.SERIALIZER.entry("url", dp -> dp.url),
            PresetParameter.SERIALIZER
                .<DatabasePreset>entry("username", dp -> dp.username)
                .orElse(PresetParameter.EMPTY),
            PresetParameter.SERIALIZER
                .<DatabasePreset>entry("password", dp -> dp.password)
                .orElse(PresetParameter.EMPTY),
            PresetParameter.SERIALIZER
                .<DatabasePreset>entry("database", dp -> dp.database)
                .orElse(PresetParameter.EMPTY),
            PresetParameter.SERIALIZER
                .<DatabasePreset>entry("table_prefix", dp -> dp.tablePrefix)
                .orElse(PresetParameter.EMPTY),
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
            SERIALIZER.deserialize(ConfigContext.INSTANCE, config);
        if (!decoded.isComplete()) {
            return this;
        }

        return decoded.getOrNull();
    }
}
