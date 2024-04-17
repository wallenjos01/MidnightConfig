package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.ByteBufferInputStream;
import org.wallentines.mdcfg.ConfigBlob;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an SQL data type
 * @param <T> The corresponding java type
 */
public class DataType<T> {

    private final JDBCType parent;
    private final Serializer<T> serializer;
    protected final Reader<T> reader;
    protected final Writer<T> writer;

    public DataType(JDBCType parent, Reader<T> reader, Writer<T> writer, Serializer<T> serializer) {
        this.parent = parent;
        this.reader = reader;
        this.writer = writer;
        this.serializer = serializer;
    }

    /**
     * Creates a value with this type
     * @param value The raw value
     * @return A new data value
     */
    public DataValue<T> create(T value) {
        return new DataValue<>(this, value);
    }

    /**
     * Returns the internal JDBC type associated with this value
     * @return The internal JDBC type
     */
    public JDBCType getSQLType() {
        return parent;
    }

    /**
     * Gets the type name
     * @return The type name
     */
    public String getName() {
        return parent.getName();
    }

    /**
     * Gets a serializer for the corresponding Java type
     * @return A serializer
     */
    public Serializer<T> getSerializer() {
        return serializer;
    }

    /**
     * Reads a value of this type from the given result set at the given index
     * @param set The result set to read from
     * @param index The index to read from
     * @return A new data value
     * @throws SQLException If reading fails
     */
    public DataValue<T> read(ResultSet set, int index) throws SQLException {
        return new DataValue<>(this, reader.read(set, index));
    }

    /**
     * Reads a value of this type from the given result set at the given column name
     * @param set The result set to read from
     * @param columnName The column name to read from
     * @return A new data value
     * @throws SQLException If reading fails
     */
    public DataValue<T> read(ResultSet set, String columnName) throws SQLException {
        return new DataValue<>(this, reader.read(set, columnName));
    }

    public SerializeResult<DataValue<T>> deserialize(SerializeContext<T> ctx, T value) {
        return serializer.deserialize(ctx, value).flatMap(raw -> new DataValue<>(this, raw));
    }

    public static final Map<Integer, DataType<?>> REGISTRY = new HashMap<>();

    /**
     * Gets the data type with the given ID
     * @param id The ID to lookup
     * @return The type with the given ID, or null if no type with that index could be found
     */
    public static DataType<?> get(int id) {
        return REGISTRY.get(id);
    }

    private static <T> DataType<T> register(DataType<T> type) {
        REGISTRY.put(type.getSQLType().getVendorTypeNumber(), type);
        return type;
    }
    
    public static final DataType<Boolean> BIT = register(new DataType<>(JDBCType.BIT, Reader.BOOLEAN, Writer.BOOLEAN, Serializer.BOOLEAN));
    public static final DataType<Byte> TINYINT = register(new DataType<>(JDBCType.TINYINT, Reader.BYTE, Writer.BYTE, Serializer.BYTE));
    public static final DataType<Short> SMALLINT = register(new DataType<>(JDBCType.SMALLINT, Reader.SHORT, Writer.SHORT, Serializer.SHORT));
    public static final DataType<Integer> INTEGER = register(new DataType<>(JDBCType.INTEGER, Reader.INTEGER, Writer.INTEGER, Serializer.INT));
    public static final DataType<Long> BIGINT = register(new DataType<>(JDBCType.BIGINT, Reader.LONG, Writer.LONG, Serializer.LONG));
    public static final DataType<Float> REAL = register(new DataType<>(JDBCType.REAL, Reader.FLOAT, Writer.FLOAT, Serializer.FLOAT));
    public static final DataType<Double> DOUBLE = register(new DataType<>(JDBCType.DOUBLE, Reader.DOUBLE, Writer.DOUBLE, Serializer.DOUBLE));
    public static final DataType<BigDecimal> DECIMAL = register(new DataType<>(JDBCType.DECIMAL, Reader.DECIMAL, Writer.DECIMAL, Serializer.BIG_DECIMAL));
    public static final DataType<String> CHAR = register(new DataType<>(JDBCType.CHAR, Reader.STRING, Writer.STRING, Serializer.STRING));
    public static final DataType<String> VARCHAR = register(new DataType<>(JDBCType.VARCHAR, Reader.STRING, Writer.STRING, Serializer.STRING));
    public static final DataType<String> LONGVARCHAR = register(new DataType<>(JDBCType.LONGVARCHAR, Reader.STRING, Writer.STRING, Serializer.STRING));
    public static final DataType<ByteBuffer> BLOB = register(new DataType<>(JDBCType.BLOB, Reader.BLOB, Writer.BLOB, Serializer.BLOB));
    public static final DataType<Boolean> BOOLEAN = register(new DataType<>(JDBCType.BOOLEAN, Reader.BOOLEAN, Writer.BOOLEAN, Serializer.BOOLEAN));


    public static ColumnType<BigDecimal> DECIMAL(int precision, int scale) {
        return new ColumnType<>(DECIMAL, precision, scale);
    }

    public static ColumnType<String> CHAR(int length) {
        return new ColumnType<>(CHAR, length);
    }

    public static ColumnType<String> VARCHAR(int length) {
        return new ColumnType<>(VARCHAR, length);
    }

    public static ColumnType<ByteBuffer> BLOB(int length) {
        return new ColumnType<>(BLOB, length);
    }


    public interface Reader<T> {
        T read(ResultSet set, int index) throws SQLException;
        T read(ResultSet set, String column) throws SQLException;

        Reader<String> STRING = new Reader<String>() {
            @Override
            public String read(ResultSet set, int index) throws SQLException { return set.getString(index); }
            @Override
            public String read(ResultSet set, String column) throws SQLException { return set.getString(column); }
        };

        Reader<Boolean> BOOLEAN = new Reader<Boolean>() {
            @Override
            public Boolean read(ResultSet set, int index) throws SQLException { return set.getBoolean(index); }
            @Override
            public Boolean read(ResultSet set, String column) throws SQLException { return set.getBoolean(column); }
        };

        Reader<Byte> BYTE = new Reader<Byte>() {
            @Override
            public Byte read(ResultSet set, int index) throws SQLException { return set.getByte(index); }
            @Override
            public Byte read(ResultSet set, String column) throws SQLException { return set.getByte(column); }
        };

        Reader<Short> SHORT = new Reader<Short>() {
            @Override
            public Short read(ResultSet set, int index) throws SQLException { return set.getShort(index); }
            @Override
            public Short read(ResultSet set, String column) throws SQLException { return set.getShort(column); }
        };

        Reader<Integer> INTEGER = new Reader<Integer>() {
            @Override
            public Integer read(ResultSet set, int index) throws SQLException { return set.getInt(index); }
            @Override
            public Integer read(ResultSet set, String column) throws SQLException { return set.getInt(column); }
        };

        Reader<Long> LONG = new Reader<Long>() {
            @Override
            public Long read(ResultSet set, int index) throws SQLException { return set.getLong(index); }
            @Override
            public Long read(ResultSet set, String column) throws SQLException { return set.getLong(column); }
        };

        Reader<Float> FLOAT = new Reader<Float>() {
            @Override
            public Float read(ResultSet set, int index) throws SQLException { return set.getFloat(index); }
            @Override
            public Float read(ResultSet set, String column) throws SQLException { return set.getFloat(column); }
        };

        Reader<Double> DOUBLE = new Reader<Double>() {
            @Override
            public Double read(ResultSet set, int index) throws SQLException { return set.getDouble(index); }
            @Override
            public Double read(ResultSet set, String column) throws SQLException { return set.getDouble(column); }
        };

        Reader<BigDecimal> DECIMAL = new Reader<BigDecimal>() {
            @Override
            public BigDecimal read(ResultSet set, int index) throws SQLException { return set.getBigDecimal(index); }
            @Override
            public BigDecimal read(ResultSet set, String column) throws SQLException { return set.getBigDecimal(column); }
        };


        Reader<ByteBuffer> BLOB = new Reader<ByteBuffer>() {

            private ByteBuffer convertBlob(Blob b) {
                try {
                    return ConfigBlob.read(b.getBinaryStream()).getData();
                } catch (SQLException | IOException ex) {
                    throw new IllegalArgumentException("Unable to read blob!", ex);
                }
            }

            @Override
            public ByteBuffer read(ResultSet set, int index) throws SQLException {
                return convertBlob(set.getBlob(index));
            }
            @Override
            public ByteBuffer read(ResultSet set, String column) throws SQLException {
                return convertBlob(set.getBlob(column));
            }
        };
    }


    public interface Writer<T> {
        void write(PreparedStatement stmt, int index, T value) throws SQLException;

        Writer<String> STRING = PreparedStatement::setString;
        Writer<Boolean> BOOLEAN = PreparedStatement::setBoolean;
        Writer<Byte> BYTE = PreparedStatement::setByte;
        Writer<Short> SHORT = PreparedStatement::setShort;
        Writer<Integer> INTEGER = PreparedStatement::setInt;
        Writer<Long> LONG = PreparedStatement::setLong;
        Writer<Float> FLOAT = PreparedStatement::setFloat;
        Writer<Double> DOUBLE = PreparedStatement::setDouble;
        Writer<BigDecimal> DECIMAL = PreparedStatement::setBigDecimal;

        Writer<ByteBuffer> BLOB = (stmt, index, value) -> {
            Blob b = stmt.getConnection().createBlob();
            byte[] copyBuffer = new byte[1024];
            try (OutputStream os = b.setBinaryStream(1L);
                 InputStream is = new ByteBufferInputStream(value)) {
                int read;
                while ((read = is.read(copyBuffer)) != -1) {
                    os.write(copyBuffer, 0, read);
                }
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to write blob!");
            }
        };
    }

}
