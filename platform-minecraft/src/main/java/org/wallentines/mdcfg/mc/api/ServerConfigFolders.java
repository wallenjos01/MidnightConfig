package org.wallentines.mdcfg.mc.api;

import net.minecraft.server.MinecraftServer;
import org.wallentines.mdcfg.codec.FileCodecRegistry;
import org.wallentines.mdcfg.mc.impl.ServerConfigFoldersImpl;

import java.nio.file.Path;

public interface ServerConfigFolders {

    FileCodecRegistry FILE_CODEC_REGISTRY = new FileCodecRegistry();

    static Path getConfigFolder(MinecraftServer server) {
        return ServerConfigFoldersImpl.getConfigFolder(server);
    }

    static Path getGlobalConfigFolder() {
        return ServerConfigFoldersImpl.getGlobalConfigFolder();
    }

}
