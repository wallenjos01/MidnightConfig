package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.ByteBufferInputStream;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Represents a data value for use in SQL statements and query results
 * @param <T> The type of value
 */
public class DataValue<T> {

    private final DataType<T> type;
    private final T value;

    /**
     * Creates a new data value with the given associated type
     * @param type The data type
     * @param value The value
     */
    public DataValue(DataType<T> type, T value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Gets the type of data
     * @return The data type
     */
    public DataType<T> getType() {
        return type;
    }

    /**
     * Gets the value
     * @return The value
     */
    public T getValue() {
        return value;
    }

    /**
     * Writes the value to the given PreparedStatement argument at the given index
     * @param statement The prepared statement
     * @param index The argument index
     * @throws SQLException If the statement does not accept a parameter at the given index, or the data type is not supported in the current database.
     */
    public void write(PreparedStatement statement, int index) throws SQLException {
        type.writer.write(statement, index, value);
    }

    /**
     * Serializes this value using the given context
     * @param ctx The serialize context
     * @return The result of serializing this value
     * @param <O> The serialized type
     */
    public <O> SerializeResult<O> serialize(SerializeContext<O> ctx) {
        return getType().getSerializer().serialize(ctx, getValue());
    }

    /**
     * Sets the value into a ConfigSection using the given name
     * @param sec The section to set
     * @param columnName The name of the key to set
     */
    public void setConfig(ConfigSection sec, String columnName) {
        sec.set(columnName, getValue(), getType().getSerializer());
    }

    /**
     * Writes a serialized value to the given prepared statement at the given index
     * @param ctx The context associated with the serialized value
     * @param value The serialized value
     * @param statement The prepared statement to write to
     * @param index The index of the argument of the prepared statement
     * @param <T> The type of serialized object
     */
    public static <T> void writeSerialized(SerializeContext<T> ctx, T value, PreparedStatement statement, int index) {
        try {
            switch (ctx.getType(value)) {
                case STRING:
                    statement.setString(index, ctx.asString(value));
                    break;

                case NUMBER:
                    Number num = ctx.asNumber(value);
                    if(num instanceof Byte) {
                        statement.setByte(index, (Byte) num);

                    } else if(num instanceof Short) {
                        statement.setShort(index, (Short) num);

                    } else if(num instanceof Integer) {
                        statement.setInt(index, (Integer) num);

                    } else if(num instanceof Long) {
                        statement.setLong(index, (Long) num);

                    } else if(num instanceof Float) {
                        statement.setFloat(index, (Float) num);

                    } else if(num instanceof Double) {
                        statement.setDouble(index, (Double) num);

                    } else if(num instanceof BigDecimal) {
                        statement.setBigDecimal(index, (BigDecimal) num);

                    } else if(num instanceof BigInteger) {
                        statement.setBigDecimal(index, new BigDecimal(num.toString()));

                    } else {
                        throw new IllegalArgumentException("Unknown number type " + num + "!");
                    }
                    break;

                case BOOLEAN:
                    statement.setBoolean(index, ctx.asBoolean(value));
                    break;

                case BLOB:
                    statement.setBlob(index, new ByteBufferInputStream(ctx.asBlob(value)));
            }
        } catch (SQLException ex) {
            throw new IllegalArgumentException("An error occurred while writing a serialized object!", ex);
        }
    }

}
