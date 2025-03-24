package org.wallentines.mdcfg.registry;


import org.wallentines.mdcfg.serializer.InlineSerializer;

/**
 * An identifier consisting of a namespace and a path
 */
@SuppressWarnings("unused")
public class Identifier {

    private static final IllegalArgumentException EXCEPTION = new IllegalArgumentException("Unable to parse Identifier!");

    private final String namespace;
    private final String path;

    /**
     * Constructs a new Identifier with the given namespace and path
     * @param namespace The namespace
     * @param path The path
     */
    public Identifier(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    /**
     * Gets the Identifier's namespace
     * @return The namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Gets the Identifier's path
     * @return The path
     */
    public String getPath() {
        return path;
    }

    /**
     * Parses an identifier from a string in the format "namespace:path"
     * @param toParse The string to parse
     * @return A new identifier
     * @throws IllegalArgumentException If the string is in the wrong format
     */
    public static Identifier parse(String toParse) throws IllegalArgumentException {

        if(toParse == null) throw EXCEPTION;

        if(!toParse.contains(":")) {
            throw EXCEPTION;
        }

        String[] ss = toParse.split(":");
        if(ss.length != 2) {
            throw EXCEPTION;
        }

        return new Identifier(ss[0], ss[1]);
    }

    /**
     * Parses an identifier from a string, using the given default namespace as a namespace if necessary
     * @param toParse The string to parse
     * @param defaultNamespace The default namespace to use if none is found
     * @return A new identifier
     * @throws IllegalArgumentException if the string is null or in the wrong format
     */
    public static Identifier parseOrDefault(String toParse, String defaultNamespace) throws IllegalArgumentException {

        if(toParse == null) throw EXCEPTION;

        String[] ss = toParse.trim().split(":");

        // Throw an error if there are too many colons
        if(ss.length == 0 || ss.length > 2) {
            throw EXCEPTION;
        }

        // Throw an error if there is not a valid path
        if(ss.length == 2 && ss[1].isEmpty()) {
            throw EXCEPTION;
        }

        if(ss.length == 2 && ss[0].isEmpty()) {
            return new Identifier(defaultNamespace, ss[1]);
        }

        return ss.length == 1 ? new Identifier(defaultNamespace, ss[0]) : new Identifier(ss[0], ss[1]);
    }

    @Override
    public boolean equals(Object obj) {

        if(obj instanceof Identifier) {

            Identifier id = (Identifier) obj;
            return id.namespace.equals(namespace) && id.path.equals(path);

        } else {

            try {
                Identifier id = parse(obj.toString());
                return equals(id);

            } catch(IllegalArgumentException ex) {
                return false;
            }
        }
    }

    private static boolean isValid(String[] values) {

        return values.length > 1 && values[0] != null && !values[0].isEmpty() && values[1] != null && !values[1].isEmpty();
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public static InlineSerializer<Identifier> serializer(String defaultNamespace) {
        return InlineSerializer.of(Identifier::toString, str -> Identifier.parseOrDefault(str, defaultNamespace));
    }

}
