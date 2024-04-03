package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigPrimitive;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ColumnType {

    private final String encoded; // i.e. VARCHAR(256)
    private final Reader reader;

    public ColumnType(String encoded, Reader reader) {
        this.encoded = encoded;
        this.reader = reader;
    }

    public String getEncoded() {
        return encoded;
    }

    public ConfigObject read(ResultSet results, String column) {
        try {
            return reader.get(results, column);
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to read type " + encoded + " from column " + column + "!");
        }
    }

    public static ColumnType CHAR(int length) {
        if(length > Byte.MAX_VALUE || length < Byte.MIN_VALUE) throw new IllegalArgumentException("Invalid VARCHAR length! " + length);
        return new ColumnType("CHAR(" + length + ")", Reader.STRING);
    }

    public static ColumnType VARCHAR(int length) {
        if(length > Short.MAX_VALUE || length < Short.MIN_VALUE) throw new IllegalArgumentException("Invalid VARCHAR length! " + length);
        return new ColumnType("VARCHAR(" + length + ")", Reader.STRING);
    }

    public static final ColumnType BOOL = new ColumnType("BOOL", Reader.BOOLEAN);
    public static final ColumnType TINYINT = new ColumnType("TINYINT", Reader.BYTE);
    public static final ColumnType SMALLINT = new ColumnType("SMALLINT", Reader.SHORT);
    public static final ColumnType MEDIUMINT = new ColumnType("MEDIUMINT", Reader.INT);
    public static final ColumnType INT = new ColumnType("INT", Reader.INT);
    public static final ColumnType BIGINT = new ColumnType("BIGINT", Reader.LONG);
    public static ColumnType FLOAT(int digits, int decimalDigits) {
        return new ColumnType("FLOAT(" + digits + "," + decimalDigits + ")", Reader.FLOAT);
    }
    public static ColumnType DOUBLE(int digits, int decimalDigits) {
        return new ColumnType("DOUBLE(" + digits + "," + decimalDigits + ")", Reader.DOUBLE);
    }
    public static ColumnType DECIMAL(int digits, int decimalDigits) {
        if(digits > 65 || digits < 1) throw new IllegalArgumentException("Invalid DECIMAL size! " + digits);
        if(decimalDigits > 30 || decimalDigits < 1) throw new IllegalArgumentException("Invalid DECIMAL post-decimal point digits! " + decimalDigits);
        return new ColumnType("DECIMAL(" + digits + "," + decimalDigits + ")", Reader.DOUBLE);
    }


    public interface Reader {
        ConfigObject get(ResultSet set, String str) throws SQLException;

        Reader STRING = (set, str) -> new ConfigPrimitive(set.getString(str));
        Reader NSTRING = (set, str) -> new ConfigPrimitive(set.getNString(str));
        Reader BOOLEAN = (set, str) -> new ConfigPrimitive(set.getBoolean(str));
        Reader BYTE = (set, str) -> new ConfigPrimitive(set.getByte(str));
        Reader SHORT = (set, str) -> new ConfigPrimitive(set.getShort(str));
        Reader INT = (set, str) -> new ConfigPrimitive(set.getInt(str));
        Reader LONG = (set, str) -> new ConfigPrimitive(set.getLong(str));
        Reader FLOAT = (set, str) -> new ConfigPrimitive(set.getFloat(str));
        Reader DOUBLE = (set, str) -> new ConfigPrimitive(set.getDouble(str));

    }

}
