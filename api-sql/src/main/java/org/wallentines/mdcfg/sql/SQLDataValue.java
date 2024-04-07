package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.ConfigBlob;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.SerializeContext;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLDataValue<T> {

    private final Writer writer;
    private final SerializeContext<T> ctx;
    private final T value;

    public SQLDataValue(Writer writer, SerializeContext<T> ctx, T value) {
        this.writer = writer;
        this.ctx = ctx;
        this.value = value;
    }

    public void write(PreparedStatement stmt, int index) {
        try {
            writer.write(ctx, value, index, stmt);
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Unable to write data value to statement!", ex);
        }
    }

    public T getValue() {
        return value;
    }

    public static SQLDataValue<ConfigObject> of(String value) {
        return new SQLDataValue<>(Writer.STRING, ConfigContext.INSTANCE, new ConfigPrimitive(value));
    }

    public static SQLDataValue<ConfigObject> ofByte(int value) {
        return new SQLDataValue<>(Writer.BYTE, ConfigContext.INSTANCE, new ConfigPrimitive(value));
    }
    public static SQLDataValue<ConfigObject> ofShort(int value) {
        return new SQLDataValue<>(Writer.STRING, ConfigContext.INSTANCE, new ConfigPrimitive(value));
    }

    public static SQLDataValue<ConfigObject> of(int value) {
        return new SQLDataValue<>(Writer.INT, ConfigContext.INSTANCE, new ConfigPrimitive(value));
    }

    public static SQLDataValue<ConfigObject> of(long value) {
        return new SQLDataValue<>(Writer.LONG, ConfigContext.INSTANCE, new ConfigPrimitive(value));
    }

    public static SQLDataValue<ConfigObject> of(float value) {
        return new SQLDataValue<>(Writer.FLOAT, ConfigContext.INSTANCE, new ConfigPrimitive(value));
    }

    public static SQLDataValue<ConfigObject> of(double value) {
        return new SQLDataValue<>(Writer.DOUBLE, ConfigContext.INSTANCE, new ConfigPrimitive(value));
    }

    public static SQLDataValue<ConfigObject> of(Boolean value) {
        return new SQLDataValue<>(Writer.BOOLEAN, ConfigContext.INSTANCE, new ConfigPrimitive(value));
    }

    public static SQLDataValue<ConfigObject> of(ConfigBlob value) {
        return new SQLDataValue<>(Writer.BLOB, ConfigContext.INSTANCE, value);
    }

    public interface Writer {
        <T> void write(SerializeContext<T> ctx, T obj, int index, PreparedStatement statement) throws SQLException;

        Writer STRING = new Writer() {
            @Override
            public <T> void write(SerializeContext<T> ctx, T obj, int index, PreparedStatement statement) throws SQLException {
                statement.setString(index, ctx.asString(obj));
            }
        };

        Writer BOOLEAN = new Writer() {
            @Override
            public <T> void write(SerializeContext<T> ctx, T obj, int index, PreparedStatement statement) throws SQLException {
                statement.setBoolean(index, ctx.asBoolean(obj));
            }
        };

        Writer BYTE = new Writer() {
            @Override
            public <T> void write(SerializeContext<T> ctx, T obj, int index, PreparedStatement statement) throws SQLException {
                statement.setByte(index, ctx.asNumber(obj).byteValue());
            }
        };

        Writer SHORT = new Writer() {
            @Override
            public <T> void write(SerializeContext<T> ctx, T obj, int index, PreparedStatement statement) throws SQLException {
                statement.setShort(index, ctx.asNumber(obj).shortValue());
            }
        };

        Writer INT = new Writer() {
            @Override
            public <T> void write(SerializeContext<T> ctx, T obj, int index, PreparedStatement statement) throws SQLException {
                statement.setInt(index, ctx.asNumber(obj).intValue());
            }
        };

        Writer LONG = new Writer() {
            @Override
            public <T> void write(SerializeContext<T> ctx, T obj, int index, PreparedStatement statement) throws SQLException {
                statement.setLong(index, ctx.asNumber(obj).longValue());
            }
        };

        Writer FLOAT = new Writer() {
            @Override
            public <T> void write(SerializeContext<T> ctx, T obj, int index, PreparedStatement statement) throws SQLException {
                statement.setFloat(index, ctx.asNumber(obj).floatValue());
            }
        };

        Writer DOUBLE = new Writer() {
            @Override
            public <T> void write(SerializeContext<T> ctx, T obj, int index, PreparedStatement statement) throws SQLException {
                statement.setDouble(index, ctx.asNumber(obj).doubleValue());
            }
        };


        Writer DECIMAL = new Writer() {
            @Override
            public <T> void write(SerializeContext<T> ctx, T obj, int index, PreparedStatement statement) throws SQLException {

                Number num = ctx.asNumber(obj);
                if(!(num instanceof BigDecimal)) {
                    num = BigDecimal.valueOf(num.doubleValue());
                }

                statement.setBigDecimal(index, (BigDecimal) num);
            }
        };

        Writer BLOB = new Writer() {
            @Override
            public <T> void write(SerializeContext<T> ctx, T obj, int index, PreparedStatement statement) throws SQLException {

                ByteBuffer buf = ctx.asBlob(obj);
                // TODO: ByteBuffer to InputStream

            }
        };

    }

}
