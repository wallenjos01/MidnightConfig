package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;

public record DatabasePreset(String driver, String url, String username, String password, String database, String tablePrefix, ConfigSection parameters) {

    public static final Serializer<DatabasePreset> SERIALIZER = ObjectSerializer.create(
            Serializer.STRING.entry("driver", DatabasePreset::driver),
            Serializer.STRING.entry("url", DatabasePreset::url),
            Serializer.STRING.entry("username", DatabasePreset::username).optional(),
            Serializer.STRING.entry("password", DatabasePreset::password).optional(),
            Serializer.STRING.entry("database", DatabasePreset::database).optional(),
            Serializer.STRING.entry("table_prefix", DatabasePreset::tablePrefix).optional(),
            ConfigSection.SERIALIZER.entry("parameters", DatabasePreset::parameters).orElse(new ConfigSection()),
            DatabasePreset::new
    );

    public DatabasePreset finalize(ConfigSection config) {

        String driver = driver();
        String url = url();
        String username = config.getOrDefault("username", username());
        String password = config.getOrDefault("password", password());
        String database = config.getOrDefault("database", database());
        String tablePrefix = config.getOrDefault("table_prefix", tablePrefix());
        ConfigSection params = config.getOptional("parameters", ConfigSection.SERIALIZER).orElse(new ConfigSection());
        params.fill(parameters());

        if (database != null) {
            url += "/" + database;
        }

        return new DatabasePreset(driver, url, username, password, database, tablePrefix, params);
    }

}
