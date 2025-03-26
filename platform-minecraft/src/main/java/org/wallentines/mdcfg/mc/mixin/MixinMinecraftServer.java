package org.wallentines.mdcfg.mc.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.*;
import org.wallentines.mdcfg.mc.impl.ServerExtension;
import org.wallentines.mdcfg.mc.impl.ServerConfigFoldersImpl;
import org.wallentines.mdcfg.sql.PresetRegistry;

import java.nio.file.Path;

@Mixin(MinecraftServer.class)
@Implements(@Interface(iface = ServerExtension.class, prefix = "mdcfg$"))
public abstract class MixinMinecraftServer {

    @Shadow public abstract boolean isDedicatedServer();

    @Shadow @Final protected LevelStorageSource.LevelStorageAccess storageSource;

    @Unique private PresetRegistry mdcfg$presetRegistry;

    public Path mdcfg$getConfigFolder() {

        if(isDedicatedServer()) {
            return ServerConfigFoldersImpl.getGlobalConfigFolder();
        }

        return storageSource.getLevelPath(ServerConfigFoldersImpl.CONFIG_DIR);
    }

    public void mdcfg$setPresetRegistry(PresetRegistry presetRegistry) {
        this.mdcfg$presetRegistry = presetRegistry;
    }

    public PresetRegistry mdcfg$getPresetRegistry() {
        return mdcfg$presetRegistry;
    }

}
