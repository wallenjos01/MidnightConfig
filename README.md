# MidnightConfig

A general-purpose configuration and serialization library for Java

<br>

## Obtaining
MidnightConfig is hosted at [https://maven.wallentines.org/](https://maven.wallentines.org/).

Example `build.gradle.kts` buildscript configuration:
```kotlin
repositories {
    maven("https://maven.wallentines.org/releases")
}
dependencies {
    implementation("org.wallentines:midnightcfg-api:2.5.0")
    implementation("org.wallentines:midnightcfg-codec-json:2.5.0")
}
```


## Basic Usage

MidnightConfig offers an easy way to load and manage configuration files. The most important types included
in MidnightConfig are the `ConfigObject` class, the `Serializer` class, and the `Codec` class.

### ConfigObject
- The `ConfigObject` type represents configuration data which can be saved to a file. 
- `ConfigObject` is subclassed into three distinct types:
  - `ConfigPrimitive`: Stores a String, Number, or Boolean
  - `ConfigList`: Stores a list of `ConfigObject` instances
  - `ConfigSection`: Stores a map of `ConfigObject` instances with String keys.
- Primitive objects such as Strings, Numbers, and Booleans can be put into and retrieved from `ConfigObject` instances
directly.

### Serializer
- A `Serializer` object converts objects from one type into another, and vice versa. They are primarily used
to turn regular objects into `ConfigObject` instances, so they can be stored in `ConfigSection` objects.
- `Serializer` instances can be passed to the `get()` and `set()` methods of the `ConfigSection` class to
easily put regular objects into the `ConfigObject`, or get one out.
- A number of default `Serializer` objects exist in the `Serializer.class` file, including ones for primitives
such as Strings, Integers, Doubles, Booleans, and more.
- It should be possible to create a `Serializer` subclass for any object you need to store in a configuration file.
  - The `ObjectSerializer` class contains a number of useful functions for creating complex custom `Serializer`s.

### Codec
- A `Codec` object is responsible for taking configuration data and encoding it into a byte stream which can be
stored on disk, and the reverse.
- By default, there are three `Codec` implementations: 
  - The `JSONCodec` class located in the `codec-json` submodule, which can save configuration data in JSON format.
    - The `JSONCodec` class contains two variations, constructable via static methods: `readable()` and `minified()`
    - The `readable()` function will return a JSON Codec which adds a newline and indent after each value. It is, as
  the name suggests, meant for writing files that will be edited by users.
    - The `minified()` function will return a JSON Codec with no whitespace. It uses space most efficiently, but is hard
  to read. It is meant for data that will be not be edited by users.
  - The `GsonCodec` class located in the `codec-gson` submodule, which can save configuration data in JSON format using 
    Google's [Gson](https://github.com/google/gson/) ([License](https://github.com/google/gson/blob/main/LICENSE)).
    - The `GsonCodec` class contains the same two variations as the JSON Codec
    - The `codec-gson` submodule also contains a `GsonContext` class, which allows converting `ConfigSection` objects 
    to Gson primitives
  - The `BinaryCodec` class located in the `codec-binary` submodule, which is designed to save optionally-compressed,
    quick to decode binary forms of config objects
    - The `BinaryCodec` class supports two different types of compression. Deflate, which uses Java's built-in deflate
      algorithm, and Zstd, which uses [zstd-jni](https://github.com/luben/zstd-jni) ([License](https://github.com/luben/zstd-jni/blob/master/LICENSE))

<br>

## Examples

Creating a new `ConfigSection` and putting data in it:
```
ConfigSection config = new ConfigSection();
config.set("key", "value");
config.set("number", 42);
```

Reading data from a `ConfigSection`:
```
String value = config.getString("key");
int number = config.getInt("number");
```

Reading raw data from a `ConfigSection`:
```
ConfigObject obj = config.get("key");
```

Reading data from a `ConfigSection` with a default fallback value:
```
String defaulted = config.getOrDefault("invalid_key", "default");
```

Reading optional data from a `ConfigSection`:
```
Optional<ConfigObject> optional = config.getOptional("invalid_key");
```

Creating a class with a custom `Serializer` via `ObjectSerializer`:
```
class MyClass {
    private final String str;
    private final int num;
    
    MyClass(String str, int num) {
        this.str = str;
        this.num = num;
    }
    
    public String getString() { return str; }
    public int getNumber() { return num; }
    
    public static final Serializer<MyClass> SERIALIZER = ObjectSerializer.create(
        Serializer.STRING.entry("str", MyClass::getString),
        Serializer.INT.entry("num", MyClass::getNumber),
        MyClass::new
    );
}
```

Using that serializer to store objects in a `ConfigSection`:
```
MyClass myObj = new MyClass("Hello", 17);
config.set("my_object", myObj, MyClass.SERIALIZER);
```

Using that serializer to retrieve objects from a `ConfigSection`:
```
MyClass deserialized = config.get("my_object", MyClass.SERIALIZER);
```

Saving a `ConfigSection` to a JSON string:
```
String json = JSONCodec.readable().encodeToString(ConfigContext.INSTANCE, config);
```

Loading a `ConfigSection` from a JSON string:
```
ConfigSection decoded = JSONCodec.readable().decode(ConfigContext.INSTANCE, json).asSection();
```

Saving a `ConfigSection` in a compressed binary format:
```
FileCodec codec = BinaryCodec.fileCodec(BinaryCodec.Compression.ZSTD);
codec.saveToFile(ConfigContext.INSTANCE, section, new File("test.mdb"), null);
```
