package org.wallentines.mdcfg.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;

/**
 * A {@link org.wallentines.mdcfg.serializer.SerializeContext SerializeContext} for serializing to or deserializing from
 * GSON objects
 */
public class GsonContext implements SerializeContext<JsonElement> {

    /**
     * The global GSON Context instance
     */
    public static final GsonContext INSTANCE = new GsonContext();

    @Override
    public String asString(JsonElement object) {
        if(!isString(object)) return null;
        return object.getAsString();
    }

    @Override
    public Number asNumber(JsonElement object) {
        if(!isNumber(object)) return null;
        return object.getAsNumber();
    }

    @Override
    public Boolean asBoolean(JsonElement object) {
        if(!isBoolean(object)) return null;
        return object.getAsBoolean();
    }

    @Override
    public ByteBuffer asBlob(JsonElement object) {
        return null;
    }

    @Override
    public Collection<JsonElement> asList(JsonElement object) {
        if(!isList(object)) return null;
        return object.getAsJsonArray().asList();
    }

    @Override
    public Map<String, JsonElement> asMap(JsonElement object) {
        if(!isMap(object)) return null;
        return object.getAsJsonObject().asMap();
    }

    @Override
    public Map<String, JsonElement> asOrderedMap(JsonElement object) {
        if(!isMap(object)) return null;
        return object.getAsJsonObject().asMap();
    }

    @Override
    public boolean isString(JsonElement object) {
        return object != null && object.isJsonPrimitive() && object.getAsJsonPrimitive().isString();
    }

    @Override
    public boolean isNumber(JsonElement object) {
        return object != null && object.isJsonPrimitive() && object.getAsJsonPrimitive().isNumber();
    }

    @Override
    public boolean isBoolean(JsonElement object) {
        return object != null && object.isJsonPrimitive() && object.getAsJsonPrimitive().isBoolean();
    }

    @Override
    public boolean isList(JsonElement object) {
        return object != null && object.isJsonArray();
    }

    @Override
    public boolean isMap(JsonElement object) {
        return object != null && object.isJsonObject();
    }

    @Override
    public Type getType(JsonElement object) {
        if(object == null) return Type.UNKNOWN;
        if(object.isJsonPrimitive()) {
            JsonPrimitive prim = object.getAsJsonPrimitive();
            if(prim.isString()) return Type.STRING;
            if(prim.isNumber()) return Type.NUMBER;
            if(prim.isBoolean()) return Type.BOOLEAN;
        }
        if(object.isJsonArray()) return Type.LIST;
        if(object.isJsonObject()) return Type.MAP;
        if(object.isJsonNull()) return Type.NULL;

        throw new IllegalStateException("Unable to determine type of " + object);
    }

    @Override
    public Collection<String> getOrderedKeys(JsonElement object) {
        if(!isMap(object)) return null;
        return object.getAsJsonObject().keySet();
    }

    @Override
    public JsonElement get(String key, JsonElement object) {
        if(!isMap(object)) return null;
        return object.getAsJsonObject().get(key);
    }

    @Override
    public JsonElement toString(String object) {
        return object == null ? null : new JsonPrimitive(object);
    }

    @Override
    public JsonElement toNumber(Number object) {
        return object == null ? null : new JsonPrimitive(object);
    }

    @Override
    public JsonElement toBoolean(Boolean object) {
        return object == null ? null : new JsonPrimitive(object);
    }

    @Override
    public JsonElement toBlob(ByteBuffer object) {
        return null;
    }

    @Override
    public JsonElement toList(Collection<JsonElement> list) {
        JsonArray arr = new JsonArray();
        if(list != null) list.forEach(v -> {
            if(v != null) arr.add(v);
        });
        return arr;
    }

    @Override
    public JsonElement toMap(Map<String, JsonElement> map) {
        JsonObject obj = new JsonObject();
        if(map != null) map.forEach((k,v) -> {
            if(v != null) obj.add(k,v);
        });
        return obj;
    }

    @Override
    public JsonElement mergeList(Collection<JsonElement> list, JsonElement object) {
        if(!isList(object)) return null;
        JsonArray arr = object.getAsJsonArray();
        if(list != null) list.forEach(arr::add);
        return arr;
    }

    @Override
    public JsonElement mergeMap(JsonElement object, JsonElement other) {
        if(object == null || !object.isJsonObject() || other == null || !other.isJsonObject()) return null;

        JsonObject base = object.getAsJsonObject();
        JsonObject fill = other.getAsJsonObject();

        for(String key : fill.keySet()) {
            JsonElement value = fill.get(key);
            if(value != null && !base.has(key)) base.add(key, fill.get(key));
        }
        return object;
    }

    @Override
    public JsonElement mergeMapOverwrite(JsonElement object, JsonElement other) {
        if(object == null || !object.isJsonObject() || other == null || !other.isJsonObject()) return null;

        JsonObject base = object.getAsJsonObject();
        JsonObject fill = other.getAsJsonObject();

        for(String key : fill.keySet()) {
            JsonElement value = fill.get(key);
            if(value != null) {
                base.add(key, value);
            }
        }
        return object;
    }

    @Override
    public JsonElement set(String key, JsonElement value, JsonElement object) {
        if(object == null || !object.isJsonObject()) return null;
        if(value == null) {
            object.getAsJsonObject().remove(key);
        } else {
            object.getAsJsonObject().add(key, value);
        }
        return object;
    }

    @Override
    public boolean supportsMeta(JsonElement object) {
        return false;
    }

    @Override
    public String getMetaProperty(JsonElement object, String key) {
        return null;
    }

    @Override
    public void setMetaProperty(JsonElement object, String key, String value) { }

}
