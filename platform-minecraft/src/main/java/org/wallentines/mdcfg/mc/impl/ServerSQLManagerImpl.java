package org.wallentines.mdcfg.mc.impl;

import net.minecraft.server.MinecraftServer;
import org.wallentines.mdcfg.sql.PresetRegistry;

public class ServerSQLManagerImpl {

    public static PresetRegistry getPresetRegistry(MinecraftServer server) {
        return ((ServerExtension) server).getPresetRegistry();
    }

}
