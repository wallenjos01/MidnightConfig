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

    public void loadDriver(String name) throws IOException {

        DriverSpec spec = registry.get(name);
        if(spec.loaded) return;

        spec.finalizeVersion();

        String fileName = spec.artifact.getNamespace() + "." + spec.artifact.getId() + "." + spec.artifact.getVersion() + ".jar";
        File file = new File(folder, fileName);

        if(!file.exists()) {
            MavenUtil.downloadArtifact(spec.repository, spec.artifact, file);
        }

        try(URLClassLoader loader = new URLClassLoader(new URL[] { file.toURI().toURL() }, getClass().getClassLoader())) {

            InputStream is = loader.getResourceAsStream("/META-INF/services/java.sql.Driver");
            if(is == null) {
                throw new IOException("Downloaded jar does not have an SQL driver!");
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String str = br.readLine();

            try {
                loader.loadClass(str);
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException("Unable to find driver class!");
            }

            spec.loaded = true;
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

        void finalizeVersion() {

            if(artifact.getVersion() == null) {
                artifact = artifact.withVersion(MavenUtil.getLatestVersion(repository, artifact));
            }
        }

    }

}
