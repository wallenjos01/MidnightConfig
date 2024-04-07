package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.serializer.SerializeContext;

import java.nio.ByteBuffer;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ColumnType {

    private final String encoded; // i.e. VARCHAR(256)
    private final Reader reader;
    private final SQLDataValue.Writer writer;


    public ColumnType(String encoded, Reader reader, SQLDataValue.Writer writer) {
        this.encoded = encoded;
        this.reader = reader;
        this.writer = writer;
    }

    public String getEncoded() {
        return encoded;
    }

    public <T> SQLDataValue<T> read(SerializeContext<T> ctx, ResultSet results, String column) {
        try {
            return new SQLDataValue<>(writer, ctx, reader.get(ctx, results, column));
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to read type " + encoded + " from column " + column + "!");
        }
    }

    public <T> SQLDataValue<T> create(SerializeContext<T> ctx, T value) {
        return new SQLDataValue<>(writer, ctx, value);
    }

    public SQLDataValue.Writer getWriter() {
        return writer;
    }

    // Numbers
    public static final ColumnType BOOL = new ColumnType("BOOL", Reader.BOOLEAN, SQLDataValue.Writer.BOOLEAN);
    public static final ColumnType TINYINT = new ColumnType("TINYINT", Reader.BYTE, SQLDataValue.Writer.NUMBER);
    public static final ColumnType SMALLINT = new ColumnType("SMALLINT", Reader.SHORT, SQLDataValue.Writer.NUMBER);
    public static final ColumnType MEDIUMINT = new ColumnType("MEDIUMINT", Reader.INT, SQLDataValue.Writer.NUMBER);
    public static final ColumnType INT = new ColumnType("INT", Reader.INT, SQLDataValue.Writer.NUMBER);
    public static final ColumnType BIGINT = new ColumnType("BIGINT", Reader.LONG, SQLDataValue.Writer.NUMBER);
    public static ColumnType FLOAT(int precision) {
        return new ColumnType("FLOAT(" + precision + ")", Reader.FLOAT, SQLDataValue.Writer.NUMBER);
    }

    public static ColumnType DECIMAL(int digits, int precision) {
        if(digits > 65 || digits < 1) throw new IllegalArgumentException("Invalid DECIMAL size! " + digits);
        if(precision > 30 || precision < 1) throw new IllegalArgumentException("Invalid DECIMAL precision digits! " + precision);
        return new ColumnType("DECIMAL(" + digits + "," + precision + ")", Reader.DOUBLE, SQLDataValue.Writer.DECIMAL(precision));
    }
    public static final ColumnType FLOAT = FLOAT(24);
    public static final ColumnType DOUBLE = FLOAT(53);

    // Strings
    public static ColumnType CHAR(int length) {
        if(length > 0xFF || length < 0) throw new IllegalArgumentException("Invalid VARCHAR length! " + length);
        return new ColumnType("CHAR(" + length + ")", Reader.STRING, SQLDataValue.Writer.STRING);
    }

    public static ColumnType VARCHAR(int length) {
        if(length > 0xFFFF || length < 0) throw new IllegalArgumentException("Invalid VARCHAR length! " + length);
        return new ColumnType("VARCHAR(" + length + ")", Reader.STRING, SQLDataValue.Writer.STRING);
    }
    public static final ColumnType TINYTEXT = new ColumnType("TINYTEXT", Reader.STRING, SQLDataValue.Writer.STRING);
    public static ColumnType TEXT(int size) {
        if(size > 0xFFFF || size < 0) throw new IllegalArgumentException("Invalid TEXT length! " + size);
        return new ColumnType("TEXT(" + size + ")", Reader.STRING, SQLDataValue.Writer.STRING);
    }
    public static ColumnType MEDIUMTEXT(int size) {
        if(size > 0xFFFFFF || size < 0) throw new IllegalArgumentException("Invalid MEDIUMTEXT length! " + size);
        return new ColumnType("MEDIUMTEXT(" + size + ")", Reader.STRING, SQLDataValue.Writer.STRING);
    }
    public static ColumnType LONGTEXT(long size) {
        if(size > 0xFFFFFFFFL || size < 0) throw new IllegalArgumentException("Invalid LONGTEXT length! " + size);
        return new ColumnType("LONGTEXT(" + size + ")", Reader.STRING, SQLDataValue.Writer.STRING);
    }

    // Blobs
    public static final ColumnType TINYBLOB = new ColumnType("TINYBLOB", Reader.BLOB, SQLDataValue.Writer.BLOB);
    public static ColumnType BLOB(int size) {
        if(size > 0xFFFF || size < 0) throw new IllegalArgumentException("Invalid BLOB length! " + size);
        return new ColumnType("BLOB(" + size + ")", Reader.BLOB, SQLDataValue.Writer.BLOB);
    }
    public static ColumnType MEDIUMBLOB(int size) {
        if(size > 0xFFFFFF || size < 0) throw new IllegalArgumentException("Invalid MEDIUMBLOB length! " + size);
        return new ColumnType("MEDIUMBLOB(" + size + ")", Reader.BLOB, SQLDataValue.Writer.BLOB);
    }
    public static ColumnType LONGBLOB(long size) {
        if(size > 0xFFFFFFFFL || size < 0) throw new IllegalArgumentException("Invalid LONGBLOB length! " + size);
        return new ColumnType("LONGBLOB(" + size + ")", Reader.BLOB, SQLDataValue.Writer.BLOB);
    }


    public interface Reader {
        <T> T get(SerializeContext<T> ctx, ResultSet set, String str) throws SQLException;

        Reader STRING = new Reader() {
            @Override
            public <T> T get(SerializeContext<T> ctx, ResultSet set, String str) throws SQLException {
                return ctx.toString(set.getString(str));
            }
        };

        Reader NSTRING = new Reader() {
            @Override
            public <T> T get(SerializeContext<T> ctx, ResultSet set, String str) throws SQLException {
                return ctx.toString(set.getNString(str));
            }
        };

        Reader BOOLEAN = new Reader() {
            @Override
            public <T> T get(SerializeContext<T> ctx, ResultSet set, String str) throws SQLException {
                return ctx.toBoolean(set.getBoolean(str));
            }
        };

        Reader BYTE = new Reader() {
            @Override
            public <T> T get(SerializeContext<T> ctx, ResultSet set, String str) throws SQLException {
                return ctx.toNumber(set.getByte(str));
            }
        };

        Reader SHORT = new Reader() {
            @Override
            public <T> T get(SerializeContext<T> ctx, ResultSet set, String str) throws SQLException {
                return ctx.toNumber(set.getShort(str));
            }
        };

        Reader INT = new Reader() {
            @Override
            public <T> T get(SerializeContext<T> ctx, ResultSet set, String str) throws SQLException {
                return ctx.toNumber(set.getInt(str));
            }
        };

        Reader LONG = new Reader() {
            @Override
            public <T> T get(SerializeContext<T> ctx, ResultSet set, String str) throws SQLException {
                return ctx.toNumber(set.getLong(str));
            }
        };

        Reader FLOAT = new Reader() {
            @Override
            public <T> T get(SerializeContext<T> ctx, ResultSet set, String str) throws SQLException {
                return ctx.toNumber(set.getFloat(str));
            }
        };

        Reader DOUBLE = new Reader() {
            @Override
            public <T> T get(SerializeContext<T> ctx, ResultSet set, String str) throws SQLException {
                return ctx.toNumber(set.getDouble(str));
            }
        };

        Reader BLOB = new Reader() {
            @Override
            public <T> T get(SerializeContext<T> ctx, ResultSet set, String str) throws SQLException {
                Blob b = set.getBlob(str);
                ByteBuffer out = ByteBuffer.allocate((int) b.length());
                out.put(b.getBytes(0, (int) b.length()));
                return ctx.toBlob(out);
            }
        };

    }


}
