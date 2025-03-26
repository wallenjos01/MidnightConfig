package org.wallentines.mdcfg.mc.api;

import net.minecraft.server.MinecraftServer;
import org.wallentines.mdcfg.mc.impl.ServerSQLManagerImpl;
import org.wallentines.mdcfg.sql.PresetRegistry;

public interface ServerSQLManager {

    static PresetRegistry getPresetRegistry(MinecraftServer server) {
        return ServerSQLManagerImpl.getPresetRegistry(server);
    }

}
