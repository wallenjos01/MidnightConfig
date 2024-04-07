package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.ConfigBlob;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.SerializeContext;

import java.text.DecimalFormat;

public class SQLDataValue<T> {

    private final Writer writer;
    private final SerializeContext<T> ctx;
    private final T value;

    public SQLDataValue(Writer writer, SerializeContext<T> ctx, T value) {
        this.writer = writer;
        this.ctx = ctx;
        this.value = value;
    }

    public String write() {
        return writer.write(ctx, value);
    }

    public T getValue() {
        return value;
    }

    public static SQLDataValue<ConfigObject> of(String value) {
        return new SQLDataValue<>(Writer.STRING, ConfigContext.INSTANCE, new ConfigPrimitive(value));
    }

    public static SQLDataValue<ConfigObject> of(Number value) {
        return new SQLDataValue<>(Writer.NUMBER, ConfigContext.INSTANCE, new ConfigPrimitive(value));
    }

    public static SQLDataValue<ConfigObject> of(Boolean value) {
        return new SQLDataValue<>(Writer.BOOLEAN, ConfigContext.INSTANCE, new ConfigPrimitive(value));
    }

    public static SQLDataValue<ConfigObject> of(ConfigBlob value) {
        return new SQLDataValue<>(Writer.BLOB, ConfigContext.INSTANCE, value);
    }

    public interface Writer {
        <T> String write(SerializeContext<T> ctx, T obj);

        Writer STRING = new Writer() {
            @Override
            public <T> String write(SerializeContext<T> ctx, T obj) {
                return "'" + ctx.asString(obj) + "'";
            }
        };

        Writer BOOLEAN = new Writer() {
            @Override
            public <T> String write(SerializeContext<T> ctx, T obj) {
                return ctx.asBoolean(obj) ? "1" : "0";
            }
        };

        Writer NUMBER = new Writer() {
            @Override
            public <T> String write(SerializeContext<T> ctx, T obj) {
                return ctx.asNumber(obj).toString();
            }
        };


        static Writer DECIMAL(int decimalDigits) {
            DecimalFormat fmt = new DecimalFormat("#." + "#".repeat(decimalDigits));
            return new Writer() {
                @Override
                public <T> String write(SerializeContext<T> ctx, T obj) {
                    Number num = ctx.asNumber(obj);
                    return fmt.format(num);
                }
            };
        }


        Writer BLOB = new Writer() {
            @Override
            public <T> String write(SerializeContext<T> ctx, T obj) {
                return ctx.asBlob(obj).asCharBuffer().toString();
            }
        };

    }

}
