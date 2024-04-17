package org.wallentines.mdcfg.sql;

import org.jetbrains.annotations.Nullable;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Stack;

/**
 * Downloads artifacts from maven repositories.
 */
public class MavenUtil {

    /**
     * Determines the latest version of the given artifact in the given repository.
     * @param repo The maven repository to search in.
     * @param spec The artifact to lookup.
     * @return The latest version of the artifact, or null if none could be found.
     */
    public static String getLatestVersion(String repo, ArtifactSpec spec) {

        String url = repo + "/" + spec.getMetadataPath();

        try {
            URL actualUrl = new URL(url);
            URLConnection conn = actualUrl.openConnection();

            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            LatestVersionHandler handler = new LatestVersionHandler();
            parser.parse(conn.getInputStream(), handler);

            return handler.latestVersion;

        } catch (IOException | ParserConfigurationException | SAXException ex) {
            return null;
        }
    }

    /**
     * Downloads the given artifact from the given repository, outputting to the given file.
     * @param repo The repository to search in.
     * @param spec The artifact to download.
     * @param output The file to output the artifact to.
     * @throws IOException If an error occurs while downloading.
     * @throws IllegalArgumentException if the artifact does not have a valid version.
     */
    public static void downloadArtifact(String repo, ArtifactSpec spec, File output) throws IOException {
        String path = spec.getArtifactPath();
        if(path == null) {
            throw new IllegalArgumentException("Unable to download artifact with missing version!");
        }
        String actualUrl = repo + "/" + path;
        downloadBytes(actualUrl, output);
    }

    private static void downloadBytes(String url, File output) throws IOException{

        URL actualUrl = new URL(url);
        URLConnection conn = actualUrl.openConnection();
        FileOutputStream outputStream = new FileOutputStream(output);

        int bytesRead;
        byte[] buffer = new byte[1024];
        while((bytesRead = conn.getInputStream().read(buffer, 0, 1024)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.close();
        output.setExecutable(true);
    }

    /**
     * Holds the namespace, id, and version of a maven artifact.
     */
    public static class ArtifactSpec {
        private final String namespace;
        private final String id;
        private final @Nullable String version;

        /**
         * Creates an artifact specification with the given namespace, id, and version.
         * @param namespace The namespace of the artifact.
         * @param id The identifier of the artifact.
         * @param version The version of the artifact.
         */
        public ArtifactSpec(String namespace, String id, @Nullable String version) {
            this.namespace = namespace;
            this.id = id;
            this.version = version;
        }

        /**
         * Gets the namespace of the artifact.
         * @return The artifact's namespace.
         */
        public String getNamespace() {
            return namespace;
        }

        /**
         * Gets the ID of the artifact.
         * @return The artifact's ID.
         */
        public String getId() {
            return id;
        }

        /**
         * Gets the version of the artifact.
         * @return The artifact's version.
         */
        public @Nullable String getVersion() {
            return version;
        }

        /**
         * Gets the path within the repository of the artifact. Only valid if the version is not null.
         * @return The artifact's file path.
         */
        public String getArtifactPath() {
            if(version == null) {
                return null;
            }
            return namespace.replace(".", "/") + String.format("/%s/%s/%s-%s.jar", id, version, id, version);
        }

        /**
         * Gets the path within the repository of the artifact's metadata file.
         * @return The artifact's metadata path.
         */
        public String getMetadataPath() {
            return namespace.replace(".", "/") + "/" + id + "/maven-metadata.xml";
        }

        /**
         * Creates an artifact specification with matching this one, with the given version.
         * @param version The artifact's version.
         * @return A new artifact spec with the given version.
         */
        public ArtifactSpec withVersion(String version) {
            return new ArtifactSpec(namespace, id, version);
        }

        /**
         * Parses an artifact spec from the given string.
         * @param input An encoded artifact spec.
         * @return A parsed artifact spec, or null if the string was not in the correct format.
         */
        public static ArtifactSpec parse(String input) {
            String[] parts = input.split(":");
            if(parts.length == 2) {
                return new ArtifactSpec(parts[0], parts[1], null);
            } else if(parts.length == 3) {
                return new ArtifactSpec(parts[0], parts[1], parts[2]);
            }
            return null;
        }

        @Override
        public String toString() {

            StringBuilder out = new StringBuilder(namespace).append(":").append(id);
            if(version != null) {
                out.append(":").append(version);
            }
            return out.toString();
        }

        public static final Serializer<ArtifactSpec> SERIALIZER = new Serializer<ArtifactSpec>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, ArtifactSpec value) {
                return SerializeResult.success(context.toString(value.toString()));
            }

            @Override
            public <O> SerializeResult<ArtifactSpec> deserialize(SerializeContext<O> context, O value) {
                if(!context.isString(value)) return SerializeResult.failure("Expected a string!");
                return SerializeResult.ofNullable(ArtifactSpec.parse(context.asString(value)), "Found malformed artifact spec!");
            }
        };
    }


    private static class LatestVersionHandler extends DefaultHandler {
        private String latestVersion = null;
        private final Stack<String> tags = new Stack<>();
        private final StringBuilder currentValue = new StringBuilder();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {

            tags.push(qName);
            currentValue.setLength(0);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            String tag = tags.pop();
            if(!tag.equals(qName)) throw new SAXException("Corrupted stack!");

            if(tag.equals("latest") && tags.peek().equals("versioning") && tags.size() == 2) {
                latestVersion = currentValue.toString();
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {

            currentValue.append(ch, start, length);
        }
    }

}
