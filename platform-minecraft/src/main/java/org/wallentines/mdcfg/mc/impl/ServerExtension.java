package org.wallentines.mdcfg.mc.impl;

import org.wallentines.mdcfg.sql.PresetRegistry;

import java.nio.file.Path;

public interface ServerExtension {

    Path getConfigFolder();

    PresetRegistry getPresetRegistry();

    void setPresetRegistry(PresetRegistry presetRegistry);

}
