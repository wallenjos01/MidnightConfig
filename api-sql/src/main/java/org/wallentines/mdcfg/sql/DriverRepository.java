package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.serializer.InlineSerializer;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Loads and manages JDBC drivers
 */
public abstract class DriverRepository {

    protected final Type type;
    protected final Map<String, DriverSpec> registry;
    private final Map<String, DatabaseType> loaded;

    protected DriverRepository(Type type, Map<String, DriverSpec> registry) {
        this.type = type;
        this.registry = Map.copyOf(registry);
        this.loaded = new ConcurrentHashMap<>();
    }

    /**
     * Gets a driver with the given name, loading it if necessary
     * @param name The name of the driver to lookup or load
     * @return A DatabaseType corresponding to the given driver name
     * @throws NoSuchDriverException If no driver spec with the given name is registered
     * @throws DriverLoadException If the driver could not be loaded
     */
    public DatabaseType getDriver(String name) {

        return loaded.computeIfAbsent(name, k -> {
            DriverSpec spec = registry.get(name);
            if(spec == null) {
                throw new NoSuchDriverException("Unable to find driver " + name + "!");
            }
            return loadDriver(name, registry.get(name));
        });
    }

    protected Type getType() {
        return type;
    }

    /**
     * Loads a driver with the given name and spec
     */
    protected abstract DatabaseType loadDriver(String name, DriverSpec spec);


    private static class DriverLoader extends URLClassLoader {

        public DriverLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        public void addURL(URL url) {
            super.addURL(url);
        }

    }

    private static class WrappedDriver implements Driver {

        private final Driver driver;

        public WrappedDriver(Driver driver) {
            this.driver = driver;
        }

        @Override
        public Connection connect(String url, Properties info) throws SQLException {
            return driver.connect(url, info);
        }

        @Override
        public boolean acceptsURL(String url) throws SQLException {
            return driver.acceptsURL(url);
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
            return driver.getPropertyInfo(url, info);
        }

        @Override
        public int getMajorVersion() {
            return driver.getMajorVersion();
        }

        @Override
        public int getMinorVersion() {
            return driver.getMinorVersion();
        }

        @Override
        public boolean jdbcCompliant() {
            return driver.jdbcCompliant();
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return driver.getParentLogger();
        }
    }

    /**
     * Loads drivers from the classpath or downloads them from the internet and stores them in a folder
     */
    protected static abstract class Disk extends DriverRepository {

        protected final Path folder;
        private final DriverLoader loader;
        /**
         * Sets up a folder driver repository in the given folder, using default driver registry
         * @param folder The folder to store drivers in
         */
        protected Disk(Type type, Path folder) {
            this(type, folder, DEFAULT_DRIVERS);
        }

        /**
         * Sets up a folder driver repository in the given folder, using the given driver registry
         * @param folder The folder to store drivers in
         * @param registry The driver specifications, and their names
         */
        protected Disk(Type type, Path folder, Map<String, DriverSpec> registry) {
            super(type, registry);
            this.folder = folder;
            this.loader = new DriverLoader(new URL[] {}, getClass().getClassLoader());
        }

        /**
         * Gets the folder drivers are stored in
         * @return The folder drivers are stored in.
         */
        public Path getFolder() {
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

            Optional<Path> file = locateFile(name, prefix, spec);
            if(file.isPresent()) {
                return loadDriver(file.get(), prefix, spec);
            }
            throw new DriverLoadException("Unable to find driver file for " + name + "!");
        }

        protected DatabaseType loadDriver(Path file, String prefix, DriverSpec spec) {

            try {

                loader.addURL(file.toUri().toURL());

                String className = spec.className;
                if(className == null) {
                    InputStream is = loader.getResourceAsStream("/META-INF/services/java.sql.Driver");
                    if (is == null) {
                        throw new DriverLoadException("Downloaded jar does not contain a JDBC driver!");
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    className = br.readLine();
                }

                try {
                    Class<?> clazz = loader.loadClass(className);
                    Driver driver = (Driver) clazz.getConstructor().newInstance();
                    WrappedDriver wd = new WrappedDriver(driver);
                    DriverManager.registerDriver(wd);

                } catch (ClassNotFoundException ex) {
                    throw new DriverLoadException("Unable to load SQL driver class!", ex);
                } catch(InvocationTargetException | InstantiationException | IllegalAccessException |
                        NoSuchMethodException ex) {
                    throw new DriverLoadException("Unable to instantiate SQL driver!", ex);
                } catch (SQLException ex) {
                    throw new DriverLoadException("Unable to register SQL driver!", ex);
                }
                return spec.factory.create(prefix);

            } catch (IOException ex) {

                throw new DriverLoadException("An error occurred while loading an SQL driver!", ex);
            }
        }


        protected abstract Optional<Path> locateFile(String name, String prefix, DriverSpec spec);
    }

    /**
     * Loads drivers from the classpath or downloads them from the internet and stores them in a folder
     */
    public static class Maven extends Disk {

        /**
         * Sets up a folder driver repository in the given folder, using default driver registry
         * @param folder The folder to store drivers in
         */
        public Maven(Path folder) {
            this(folder, DEFAULT_DRIVERS);
        }

        /**
         * Sets up a folder driver repository in the given folder, using the given driver registry
         * @param folder The folder to store drivers in
         * @param registry The driver specifications, and their names
         */
        public Maven(Path folder, Map<String, DriverSpec> registry) {
            super(Type.MAVEN, folder, registry);
        }

        @Override
        protected Optional<Path> locateFile(String name, String prefix, DriverSpec spec) {
            if(spec.artifact.getVersion() == null) {
                spec = spec.forLatestVersion();
                if(spec == null) {
                    throw new DriverLoadException("Unable to find latest version for driver " + name + "!");
                }
            }

            String fileName = spec.artifact.getNamespace() + "." + spec.artifact.getId() + "." + spec.artifact.getVersion() + ".jar";
            Path file = folder.resolve(fileName);

            if(Files.notExists(file)) {
                try {
                    MavenUtil.downloadArtifact(spec.repository, spec.artifact, file);
                } catch (IOException ex) {
                    throw new DriverLoadException("Unable to download driver " + name + "!", ex);
                }
            }
            return Optional.of(file);
        }
    }


    /**
     * Loads drivers from the classpath or downloads them from the internet and stores them in a folder
     */
    public static class Folder extends Disk {

        /**
         * Sets up a folder driver repository in the given folder, using default driver registry
         * @param folder The folder to store drivers in
         */
        public Folder(Path folder) {
            this(folder, DEFAULT_DRIVERS);
        }

        /**
         * Sets up a folder driver repository in the given folder, using the given driver registry
         * @param folder The folder to store drivers in
         * @param registry The driver specifications, and their names
         */
        public Folder(Path folder, Map<String, DriverSpec> registry) {
            super(Type.FOLDER, folder, registry);
        }

        @Override
        protected Optional<Path> locateFile(String name, String prefix, DriverSpec spec) {

            String filePrefix = spec.artifact.getNamespace() + "." + spec.artifact.getId();
            try {
                return Files.list(folder).filter(path -> path.getFileName().startsWith(filePrefix) && path.getFileName().endsWith(".jar")).findFirst();
            } catch (IOException ex) {
                return Optional.empty();
            }
        }
    }

    /**
     * Loads drivers from the classpath only
     */
    public static class Classpath extends DriverRepository {

        /**
         * Creates a classpath driver repository using the default driver registry
         */
        public Classpath() {
            super(Type.CLASSPATH, DEFAULT_DRIVERS);
        }

        /**
         * Creates a classpath driver repository using the given driver registry.
         * @param registry The drivers specifications to use to load drivers.
         */
        public Classpath(Map<String, DriverSpec> registry) {
            super(Type.CLASSPATH, registry);
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

    /**
     * Represents a driver which can be loaded
     */
    public static class DriverSpec {

        private final DatabaseType.Factory factory;
        private final String prefix;
        private final String className;
        private final String repository;
        private final MavenUtil.ArtifactSpec artifact;

        public DriverSpec(DatabaseType.Factory factory, String prefix, String className, MavenUtil.ArtifactSpec artifact) {
            this(factory, prefix, className, "https://repo1.maven.org/maven2", artifact);
        }

        public DriverSpec(DatabaseType.Factory factory, String prefix, String className, String repository, MavenUtil.ArtifactSpec artifact) {
            this.factory = factory;
            this.prefix = prefix;
            this.className = className;
            this.repository = repository;
            this.artifact = artifact;
        }

        public DriverSpec forVersion(String version) {

            return new DriverSpec(factory, prefix, className, repository, artifact.withVersion(version));
        }

        public DriverSpec forLatestVersion() {

            String ver = MavenUtil.getLatestVersion(repository, artifact);
            if(ver == null) {
                return null;
            }
            return forVersion(ver);
        }
    }

    public static final Serializer<DriverSpec> DRIVER_SERIALIZER = ObjectSerializer.<DriverSpec>builder()
            .withEntry(Serializer.STRING.entry("prefix", ds -> ds.prefix))
            .withEntry(Serializer.STRING.entry("class_name", ds -> ds.className))
            .withEntry(Serializer.STRING.<DriverSpec>entry("repository", ds -> ds.repository).orElse("https://repo1.maven.org/maven2"))
            .withEntry(MavenUtil.ArtifactSpec.SERIALIZER.<DriverSpec>entry("artifact", ds -> ds.artifact).optional())
            .build(es -> SerializeResult.success(new DriverSpec(DatabaseType.Factory.DEFAULT, es.get(0), es.get(1), es.get(2), es.get(3))));

    /**
     * Contains the default supported driver specifications
     */
    public static final Map<String, DriverSpec> DEFAULT_DRIVERS = new HashMap<>();


    static {
        DEFAULT_DRIVERS.put("mysql",   new DriverSpec(DatabaseType.Factory.MYSQL, "mysql://",   "com.mysql.cj.jdbc.Driver", new MavenUtil.ArtifactSpec("com.mysql", "mysql-connector-j", null)));
        DEFAULT_DRIVERS.put("mariadb", new DriverSpec(DatabaseType.Factory.MYSQL, "mariadb://", "org.mariadb.jdbc.Driver",  new MavenUtil.ArtifactSpec("org.mariadb.jdbc", "mariadb-java-client", null)));
        //DEFAULT_DRIVERS.put("sqlite",  new DriverSpec(DatabaseType.Factory.SQLITE,  "sqlite:",    "org.sqlite.JDBC",          new MavenUtil.ArtifactSpec("org.xerial", "sqlite-jdbc", null)));
        DEFAULT_DRIVERS.put("h2",      new DriverSpec(DatabaseType.Factory.H2,      "h2:",        "org.h2.Driver",            new MavenUtil.ArtifactSpec("com.h2database", "h2", null)));
    }



    protected enum Type {

        MAVEN("maven", Maven.class, ObjectSerializer.<Maven>builder()
                .withEntry(Serializer.PATH.entry("folder", m -> m.folder))
                .withEntry(DRIVER_SERIALIZER.mapOf().<Maven>entry("drivers", f -> f.registry).orElse(DEFAULT_DRIVERS))
                .build(es -> SerializeResult.success(new Maven(es.get(0), es.get(1))))),

        FOLDER("folder", Folder.class, ObjectSerializer.<Folder>builder()
                .withEntry(Serializer.PATH.entry("folder", f -> f.folder))
                .withEntry(DRIVER_SERIALIZER.mapOf().<Folder>entry("drivers", f -> f.registry).orElse(DEFAULT_DRIVERS))
                .build(es -> SerializeResult.success(new Folder(es.get(0), es.get(1))))),

        CLASSPATH("classpath", Classpath.class, ObjectSerializer.<Classpath>builder()
                .withEntry(DRIVER_SERIALIZER.mapOf().<Classpath>entry("drivers", cp -> cp.registry).orElse(DEFAULT_DRIVERS))
                .build(es -> SerializeResult.success(new Classpath(es.get(0)))));

        private final String id;
        private final Serializer<DriverRepository> serializer;

        <T> Type(String id, Class<T> clazz, Serializer<T> serializer) {
            this.id = id;
            this.serializer = serializer.cast(clazz, DriverRepository.class);
        }

        String getId() {
            return id;
        }

        static Type byId(String id) {
            for(Type t : values()) {
                if(t.id.equals(id)) {
                    return t;
                }
            }
            return null;
        }

        static final InlineSerializer<Type> SERIALIZER = InlineSerializer.of(Type::getId, Type::byId);
    }

    public static final Serializer<DriverRepository> SERIALIZER = Type.SERIALIZER
            .fieldOf("type")
            .dispatch(t -> t.serializer, DriverRepository::getType);

}
