package org.wallentines.mdcfg.mc.impl;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.mdcfg.mc.api.ServerConfigFolders;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.sql.DatabasePreset;
import org.wallentines.mdcfg.sql.PresetRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Init implements ModInitializer {

    private static final Logger log = LoggerFactory.getLogger("MidnightConfig");

    private static final ConfigSection DEFAULTS = new ConfigSection()
            .with("repository", new ConfigSection()
                    .with("type", "maven")
                    .with("folder", "config/MidnightConfig/sql_drivers"))
            .with("presets", new ConfigSection()
                    .with("default",
                            new DatabasePreset("h2", "default", null, null, null, null, new ConfigSection()),
                            DatabasePreset.SERIALIZER
                    )
            );

    @Override
    public void onInitialize() {

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {

            Path configDir = ServerConfigFolders.createConfigFolder(server, "MidnightConfig").orElse(null);
            if(configDir == null) {
                log.error("Could not create config folder for MidnightConfig");
                return;
            }

            FileWrapper<ConfigObject> config = ServerConfigFolders.FILE_CODEC_REGISTRY.findOrCreate(ConfigContext.INSTANCE, "config", configDir, DEFAULTS);
            config.load();

            SerializeResult<PresetRegistry> reg = PresetRegistry.serializer(configDir).deserialize(ConfigContext.INSTANCE, config.getRoot());
            if(reg.isComplete()) {
                ((ServerExtension) server).setPresetRegistry(reg.getOrThrow());
            } else {
                log.error("Unable to parse preset registry!", reg.getError());
            }
        });
    }
}
