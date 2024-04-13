package org.wallentines.mdcfg.sql;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public abstract class DriverRepository {

    private final Map<String, DriverSpec> registry;
    private final Map<String, DatabaseType> loaded;

    protected DriverRepository(Map<String, DriverSpec> registry) {
        this.registry = Map.copyOf(registry);
        this.loaded = new HashMap<>();
    }

    public DatabaseType getDriver(String name) {

        return loaded.computeIfAbsent(name, k -> {
            DriverSpec spec = registry.get(name);
            if(spec == null) {
                throw new NoSuchDriverException("Unable to find driver " + name + "!");
            }
            return loadDriver(name, registry.get(name));
        });
    }

    protected abstract DatabaseType loadDriver(String name, DriverSpec spec);


    /**
     * Loads drivers from the classpath or downloads them from the internet and stores them in a folder
     */
    public static class Folder extends DriverRepository {

        private final File folder;

        public Folder(File folder) {
            this(folder, DEFAULT_DRIVERS);
        }

        protected Folder(File folder, Map<String, DriverSpec> registry) {
            super(registry);
            this.folder = folder;
        }

        public File getFolder() {
            return folder;
        }

        protected DatabaseType loadDriver(String name, DriverSpec spec) {

            String prefix = spec.prefix;
            if(prefix == null) {
                prefix = name + ":";
            }

            if(spec.className != null) {
                try {
                    Class.forName(spec.className);
                    return spec.factory.create(prefix);
                } catch (ClassNotFoundException ex) {
                    // Ignore
                }
            }

            if(spec.artifact.getVersion() == null) {
                spec = spec.forLatestVersion();
                if(spec == null) {
                    throw new DriverLoadException("Unable to find latest version for driver " + name + "!");
                }
            }

            String fileName = spec.artifact.getNamespace() + "." + spec.artifact.getId() + "." + spec.artifact.getVersion() + ".jar";
            File file = new File(folder, fileName);

            if(!file.exists()) {
                try {
                    MavenUtil.downloadArtifact(spec.repository, spec.artifact, file);
                } catch (IOException ex) {
                    throw new DriverLoadException("Unable to download driver " + name + "!", ex);
                }
            }

            try(URLClassLoader loader = new URLClassLoader(new URL[] { file.toURI().toURL() }, getClass().getClassLoader())) {

                String className = spec.className;
                if(className != null) {
                    InputStream is = loader.getResourceAsStream("/META-INF/services/java.sql.Driver");
                    if (is == null) {
                        throw new DriverLoadException("Downloaded jar does not contain a JDBC driver!");
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    className = br.readLine();
                }

                try {
                    loader.loadClass(className);
                } catch (ClassNotFoundException ex) {
                    throw new DriverLoadException("Unable to find SQL driver class!", ex);
                }
                return spec.factory.create(prefix);

            } catch (IOException ex) {

                throw new DriverLoadException("An error occurred while loading an SQL driver!", ex);
            }
        }
    }

    /**
     * Loads drivers from the classpath only
     */
    public static class Classpath extends DriverRepository {

        public Classpath() {
            super(DEFAULT_DRIVERS);
        }

        public Classpath(Map<String, DriverSpec> registry) {
            super(registry);
        }

        @Override
        protected DatabaseType loadDriver(String name, DriverSpec spec) {

            String prefix = spec.prefix;
            if(prefix == null) {
                prefix = name + ":";
            }

            if(spec.className == null) {
                throw new DriverLoadException("No driver class found for " + name + "!");
            }
            try {
                Class.forName(spec.className);
                return spec.factory.create(prefix);
            } catch (ClassNotFoundException ex) {
                throw new DriverLoadException("Unable to load driver class for " + name + "!", ex);
            }
        }
    }


    public static class DriverSpec {

        private final String repository;
        private final String prefix;
        private final String className;
        private final DatabaseType.Factory factory;
        private final MavenUtil.ArtifactSpec artifact;

        public DriverSpec(String prefix, String className, DatabaseType.Factory factory, MavenUtil.ArtifactSpec artifact) {
            this("https://repo1.maven.org/maven2/", prefix, className, factory, artifact);
        }

        public DriverSpec(String repository, String prefix, String className, DatabaseType.Factory factory, MavenUtil.ArtifactSpec artifact) {
            this.repository = repository;
            this.prefix = prefix;
            this.className = className;
            this.artifact = artifact;
            this.factory = factory;
        }

        public DriverSpec forVersion(String version) {

            return new DriverSpec(repository, prefix, className, factory, artifact.withVersion(version));
        }

        public DriverSpec forLatestVersion() {

            String ver = MavenUtil.getLatestVersion(repository, artifact);
            if(ver == null) {
                return null;
            }
            return forVersion(ver);
        }

    }

    public static final Map<String, DriverSpec> DEFAULT_DRIVERS = new HashMap<>();


    static {
        DEFAULT_DRIVERS.put("mysql",   new DriverSpec("mysql://",   "com.mysql.cj.jdbc.Driver", DatabaseType.Factory.DEFAULT, new MavenUtil.ArtifactSpec("com.mysql", "mysql-connector-j", null)));
        DEFAULT_DRIVERS.put("mariadb", new DriverSpec("mariadb://", "org.mariadb.jdbc.Driver",  DatabaseType.Factory.DEFAULT, new MavenUtil.ArtifactSpec("org.mariadb.jdbc", "mariadb-java-client", null)));
        DEFAULT_DRIVERS.put("sqlite",  new DriverSpec("sqlite:",    "org.sqlite.JDBC",          DatabaseType.Factory.DEFAULT, new MavenUtil.ArtifactSpec("org.xerial", "sqlite-jdbc", null)));
        DEFAULT_DRIVERS.put("h2",      new DriverSpec("h2:",        "org.h2.Driver",            DatabaseType.Factory.DEFAULT, new MavenUtil.ArtifactSpec("com.h2database", "h2", null)));
    }

}
