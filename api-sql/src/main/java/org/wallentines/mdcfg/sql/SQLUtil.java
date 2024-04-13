package org.wallentines.mdcfg.sql;

import java.util.regex.Pattern;

/**
 * Utilities for interacting with SQL databases
 */
public class SQLUtil {

    /**
     * A regex representing valid SQL identifiers
     */
    public static final Pattern VALID_NAME = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    /**
     * Validates the given identifier
     * @param identifier The identifier
     * @throws IllegalArgumentException If the identifier is not valid
     */
    public static void validate(String identifier) {
        if(!VALID_NAME.matcher(identifier).matches()) {
            throw new IllegalArgumentException("Invalid name " + identifier + "!");
        }
    }

}
