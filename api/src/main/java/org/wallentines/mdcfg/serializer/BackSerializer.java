package org.wallentines.mdcfg.serializer;

public interface BackSerializer<T> {

    /**
     * Deserializes an object from type O into an object of type T, using the given context
     * @param context The context by which to convert the given value to another type
     * @param value The value to deserialize
     * @return A SerializeResult containing the deserialized object, or an error String if deserialization failed
     * @param <O> The type to deserialize the value from
     */
    <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value);

}
