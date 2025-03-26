package org.wallentines.mdcfg.sql;

import org.jetbrains.annotations.Nullable;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class PresetRegistry {

    private final DriverRepository repository;
    private final Map<String, DatabasePreset> presets;
    private final Path dbRoot;

    public PresetRegistry(DriverRepository repository, Map<String, DatabasePreset> presets) {
        this.repository = repository;
        this.presets = Map.copyOf(presets);
        this.dbRoot = Paths.get(System.getProperty("user.dir"));
    }

    public PresetRegistry(DriverRepository repository, Map<String, DatabasePreset> presets, Path dbRoot) {
        this.repository = repository;
        this.presets = Map.copyOf(presets);
        this.dbRoot = dbRoot;
    }

    public DriverRepository getRepository() {
        return repository;
    }

    public Map<String, DatabasePreset> getPresets() {
        return presets;
    }

    @Nullable
    public DatabasePreset getPreset(String name) {
        return presets.get(name);
    }

    public CompletableFuture<SQLConnection> connect(DatabasePreset preset) {
        CompletableFuture<SQLConnection> out = CompletableFuture
                .supplyAsync(() -> repository.getDriver(preset.driver()).create(preset.url(), preset.username(), preset.password(), preset.tablePrefix(), preset.parameters(), dbRoot));
        out.whenComplete((conn, ex) -> {
            if (conn != null) {
                conn.close();
            }
        });
        return out;
    }

    public CompletableFuture<SQLConnection> connect(ConfigSection config) {
        String presetName = config.getOrDefault("preset", "default");
        DatabasePreset preset = getPreset(presetName);
        if(preset == null) {
            return CompletableFuture.completedFuture(null);
        }
        return connect(preset.finalize(config));
    }

    public CompletableFuture<SQLConnection> connect(DatabasePreset preset, Executor executor) {
        CompletableFuture<SQLConnection> out = CompletableFuture.supplyAsync(() -> repository.getDriver(preset.driver()).create(preset.url(), preset.username(), preset.password(), preset.tablePrefix(), preset.parameters(), dbRoot), executor);
        out.whenComplete((conn, ex) -> {
            if (conn != null) {
                conn.close();
            }
        });
        return out;
    }

    public CompletableFuture<SQLConnection> connect(ConfigSection config, Executor executor) {
        String presetName = config.getOrDefault("preset", "default");
        DatabasePreset preset = getPreset(presetName);
        if(preset == null) {
            return CompletableFuture.completedFuture(null);
        }
        return connect(preset.finalize(config), executor);
    }


    public SQLConnection connectSync(DatabasePreset preset) {
        return repository.getDriver(preset.driver()).create(preset.url(), preset.username(), preset.password(), preset.tablePrefix(), preset.parameters(), dbRoot);
    }

    public SQLConnection connectSync(ConfigSection config) {
        String presetName = config.getOrDefault("preset", "default");
        DatabasePreset preset = getPreset(presetName);
        if(preset == null) {
            return null;
        }
        return connectSync(preset.finalize(config));
    }

    public static final Serializer<PresetRegistry> SERIALIZER = serializer(Paths.get(System.getProperty("user.dir")));

    public static Serializer<PresetRegistry> serializer(Path resolveDir) {
        return ObjectSerializer.create(
                DriverRepository.SERIALIZER.entry("repository", PresetRegistry::getRepository),
                DatabasePreset.SERIALIZER.mapOf().entry("presets", PresetRegistry::getPresets),
                Serializer.PATH.<PresetRegistry>entry("resolve_from", pr -> pr.dbRoot).orElse(resolveDir),
                PresetRegistry::new
        );
    }

}
