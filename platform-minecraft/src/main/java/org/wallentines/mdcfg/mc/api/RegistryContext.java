package org.wallentines.mdcfg.mc.api;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.DelegatedContext;
import org.wallentines.mdcfg.serializer.SerializeContext;

/**
 * A SerializeContext which contains information about Minecraft registries
 * @param <T> The type of context
 */
public class RegistryContext<T> extends DelegatedContext<T, RegistryAccess> {

    public RegistryContext(SerializeContext<T> delegate, RegistryAccess value) {
        super(delegate, value);
    }

    public static RegistryContext<ConfigObject> forServer(MinecraftServer server) {
        return new RegistryContext<>(ConfigContext.INSTANCE, server.registryAccess());
    }

}
