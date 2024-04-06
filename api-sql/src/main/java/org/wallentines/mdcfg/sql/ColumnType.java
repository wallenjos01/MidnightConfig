package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.ConfigBlob;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigPrimitive;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ColumnType {

    private final String encoded; // i.e. VARCHAR(256)
    private final Reader reader;
    private final Writer writer;

    public ColumnType(String encoded, Reader reader, Writer writer) {
        this.encoded = encoded;
        this.reader = reader;
        this.writer = writer;
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

    public String write(ConfigObject obj) {
        return writer.write(obj);
    }

    // Numbers
    public static final ColumnType BOOL = new ColumnType("BOOL", Reader.BOOLEAN, Writer.BOOLEAN);
    public static final ColumnType TINYINT = new ColumnType("TINYINT", Reader.BYTE, Writer.NUMBER);
    public static final ColumnType SMALLINT = new ColumnType("SMALLINT", Reader.SHORT, Writer.NUMBER);
    public static final ColumnType MEDIUMINT = new ColumnType("MEDIUMINT", Reader.INT, Writer.NUMBER);
    public static final ColumnType INT = new ColumnType("INT", Reader.INT, Writer.NUMBER);
    public static final ColumnType BIGINT = new ColumnType("BIGINT", Reader.LONG, Writer.NUMBER);
    public static ColumnType FLOAT(int digits, int decimalDigits) {
        return new ColumnType("FLOAT(" + digits + "," + decimalDigits + ")", Reader.FLOAT, Writer.NUMBER);
    }
    public static ColumnType DOUBLE(int digits, int decimalDigits) {
        return new ColumnType("DOUBLE(" + digits + "," + decimalDigits + ")", Reader.DOUBLE, Writer.NUMBER);
    }
    public static ColumnType DECIMAL(int digits, int decimalDigits) {
        if(digits > 65 || digits < 1) throw new IllegalArgumentException("Invalid DECIMAL size! " + digits);
        if(decimalDigits > 30 || decimalDigits < 1) throw new IllegalArgumentException("Invalid DECIMAL post-decimal point digits! " + decimalDigits);
        return new ColumnType("DECIMAL(" + digits + "," + decimalDigits + ")", Reader.DOUBLE, Writer.NUMBER);
    }

    // Strings
    public static ColumnType CHAR(int length) {
        if(length > 0xFF || length < 0) throw new IllegalArgumentException("Invalid VARCHAR length! " + length);
        return new ColumnType("CHAR(" + length + ")", Reader.STRING, Writer.STRING);
    }

    public static ColumnType VARCHAR(int length) {
        if(length > 0xFFFF || length < 0) throw new IllegalArgumentException("Invalid VARCHAR length! " + length);
        return new ColumnType("VARCHAR(" + length + ")", Reader.STRING, Writer.STRING);
    }
    public static final ColumnType TINYTEXT = new ColumnType("TINYTEXT", Reader.STRING, Writer.STRING);
    public static ColumnType TEXT(int size) {
        if(size > 0xFFFF || size < 0) throw new IllegalArgumentException("Invalid TEXT length! " + size);
        return new ColumnType("TEXT(" + size + ")", Reader.STRING, Writer.STRING);
    }
    public static ColumnType MEDIUMTEXT(int size) {
        if(size > 0xFFFFFF || size < 0) throw new IllegalArgumentException("Invalid MEDIUMTEXT length! " + size);
        return new ColumnType("MEDIUMTEXT(" + size + ")", Reader.STRING, Writer.STRING);
    }
    public static ColumnType LONGTEXT(long size) {
        if(size > 0xFFFFFFFFL || size < 0) throw new IllegalArgumentException("Invalid LONGTEXT length! " + size);
        return new ColumnType("LONGTEXT(" + size + ")", Reader.STRING, Writer.STRING);
    }

    // Blobs
    public static final ColumnType TINYBLOB = new ColumnType("TINYBLOB", Reader.BLOB, Writer.BLOB);
    public static ColumnType BLOB(int size) {
        if(size > 0xFFFF || size < 0) throw new IllegalArgumentException("Invalid BLOB length! " + size);
        return new ColumnType("BLOB(" + size + ")", Reader.BLOB, Writer.BLOB);
    }
    public static ColumnType MEDIUMBLOB(int size) {
        if(size > 0xFFFFFF || size < 0) throw new IllegalArgumentException("Invalid MEDIUMBLOB length! " + size);
        return new ColumnType("MEDIUMBLOB(" + size + ")", Reader.BLOB, Writer.BLOB);
    }
    public static ColumnType LONGBLOB(long size) {
        if(size > 0xFFFFFFFFL || size < 0) throw new IllegalArgumentException("Invalid LONGBLOB length! " + size);
        return new ColumnType("LONGBLOB(" + size + ")", Reader.BLOB, Writer.BLOB);
    }


    public interface Reader {
        ConfigObject get(ResultSet set, String str) throws SQLException;

        Reader STRING = (set, str) -> ConfigPrimitive.createNullable(set.getString(str));
        Reader NSTRING = (set, str) -> ConfigPrimitive.createNullable(set.getNString(str));
        Reader BOOLEAN = (set, str) -> ConfigPrimitive.createNullable(set.getBoolean(str));
        Reader BYTE = (set, str) -> ConfigPrimitive.createNullable(set.getByte(str));
        Reader SHORT = (set, str) -> ConfigPrimitive.createNullable(set.getShort(str));
        Reader INT = (set, str) -> ConfigPrimitive.createNullable(set.getInt(str));
        Reader LONG = (set, str) -> ConfigPrimitive.createNullable(set.getLong(str));
        Reader FLOAT = (set, str) -> ConfigPrimitive.createNullable(set.getFloat(str));
        Reader DOUBLE = (set, str) -> ConfigPrimitive.createNullable(set.getDouble(str));
        Reader BLOB = (set, str) -> {
            Blob b = set.getBlob(str);
            if(b == null) return ConfigPrimitive.NULL;
            return new ConfigBlob(b.getBytes(0, (int) b.length()));
        };

    }

    public interface Writer {
        String write(ConfigObject obj);

        Writer STRING = (obj) -> "'" + obj.asString() + "'";
        Writer BOOLEAN = (obj) -> obj.asBoolean() ? "1": "0";
        Writer NUMBER = (obj) -> obj.asNumber().toString();
        Writer BLOB = (obj) -> obj.asBlob().getData().asCharBuffer().toString();

    }

}
