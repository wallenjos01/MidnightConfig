package org.wallentines.mdcfg.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wallentines.mdcfg.ConfigPrimitive;

import java.util.regex.Pattern;

public class SQLUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightConfig SQL");

    public static final Pattern VALID_NAME = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    public static  String encodePrimitive(ConfigPrimitive value) {
        if(value.isBoolean()) {
            return value.asBoolean() ? "1" : "0";
        }
        if(value.isNumber()) {
            return value.asNumber().toString();
        }
        if(value.isString()) {
            return '"' + value.asString().replace("\"", "\\\"") + '"';
        }
        throw new IllegalArgumentException("Don't know how to encode " + value + " as a SQL string!");
    }


}
