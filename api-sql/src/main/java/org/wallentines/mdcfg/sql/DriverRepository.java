package org.wallentines.mdcfg.sql;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class DriverRepository {

    private final HashMap<String, DriverSpec> registry = new HashMap<>();
    private final File folder;

    public DriverRepository(File folder, Map<String, DriverSpec> registry) {
        this.folder = folder;
        this.registry.putAll(registry);
    }

    public File getFolder() {
        return folder;
    }

    public boolean loadDriver(String name) {

        DriverSpec spec = registry.get(name);
        if(spec == null) {
            SQLUtil.LOGGER.error("Unable to find driver " + name + "!");
            return false;
        }

        if(spec.loaded) return true;

        if(!spec.finalizeVersion()) {
            SQLUtil.LOGGER.error("Unable to find latest version for driver " + name + "!");
            return false;
        }

        String fileName = spec.artifact.getNamespace() + "." + spec.artifact.getId() + "." + spec.artifact.getVersion() + ".jar";
        File file = new File(folder, fileName);

        if(!file.exists()) {
            try {
                MavenUtil.downloadArtifact(spec.repository, spec.artifact, file);
            } catch (IOException ex) {
                SQLUtil.LOGGER.error("Unable to download driver " + name + "!");
                return false;
            }
        }

        try(URLClassLoader loader = new URLClassLoader(new URL[] { file.toURI().toURL() }, getClass().getClassLoader())) {

            InputStream is = loader.getResourceAsStream("/META-INF/services/java.sql.Driver");
            if(is == null) {
                SQLUtil.LOGGER.error("Downloaded jar does not have an SQL driver!");
                return false;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String str = br.readLine();

            try {
                loader.loadClass(str);
            } catch (ClassNotFoundException ex) {
                SQLUtil.LOGGER.error("Unable to find SQL driver class!", ex);
                return false;
            }

            spec.loaded = true;
            return true;

        } catch (IOException ex) {

            SQLUtil.LOGGER.error("An error occurred while loading an SQL driver!", ex);
            return false;
        }
    }


    public static class DriverSpec {

        private final String repository;
        private MavenUtil.ArtifactSpec artifact;
        private boolean loaded = false;

        public DriverSpec(String repository, MavenUtil.ArtifactSpec artifact) {
            this.repository = repository;
            this.artifact = artifact;
        }

        boolean finalizeVersion() {

            if(artifact.getVersion() == null) {
                String ver = MavenUtil.getLatestVersion(repository, artifact);
                if(ver == null) {
                    return false;
                }
                artifact = artifact.withVersion(ver);
            }
            return true;
        }

    }

}
