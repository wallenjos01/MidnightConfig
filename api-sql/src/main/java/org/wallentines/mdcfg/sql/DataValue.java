package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.ByteBufferInputStream;
import org.wallentines.mdcfg.serializer.SerializeContext;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataValue<T> {

    private final DataType<T> type;
    private final T value;

    public DataValue(DataType<T> type, T value) {
        this.type = type;
        this.value = value;
    }

    public DataType<T> getType() {
        return type;
    }

    public T getValue() {
        return value;
    }

    public void write(PreparedStatement statement, int index) throws SQLException {
        type.writer.write(statement, index, value);
    }

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

                        statement.setObject(index, num);
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
