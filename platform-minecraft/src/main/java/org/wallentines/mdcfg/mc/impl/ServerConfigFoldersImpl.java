package org.wallentines.mdcfg.mc.impl;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.mc.api.ServerConfigFolders;

import java.nio.file.Path;

public class ServerConfigFoldersImpl {

    public static final LevelResource CONFIG_DIR = new LevelResource("config");

    public static Path getConfigFolder(MinecraftServer server) {
        return ((ServerExtension) server).getConfigFolder();
    }

    public static Path getGlobalConfigFolder() {
        return Path.of("config");
    }

    static {
        ServerConfigFolders.FILE_CODEC_REGISTRY.registerFileCodec(JSONCodec.fileCodec());
    }

}
