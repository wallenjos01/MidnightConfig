import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.*;
import org.wallentines.mdcfg.codec.BinaryCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.Serializer;

import java.nio.charset.Charset;

public class TestBlob {

    @Test
    public void testBlob() {

        ConfigSection sec = new ConfigSection()
                .with("key", "value")
                .with("num", 11)
                .with("list", new ConfigList().append("1").append(1))
                .with("section", new ConfigSection()
                        .with("key", "value2"));

        BinaryCodec codec = new BinaryCodec(BinaryCodec.Compression.ZSTD);

        Serializer<ConfigSection> serializer = ConfigSection.SERIALIZER.mapToBlob(codec);

        ConfigObject obj = serializer.serialize(ConfigContext.INSTANCE, sec).getOrNull();
        Assertions.assertNotNull(obj);
        Assertions.assertInstanceOf(ConfigBlob.class, obj);

        ConfigBlob blob = (ConfigBlob) obj;
        try {
            ConfigSection decoded = codec.decode(ConfigContext.INSTANCE, new ByteBufferInputStream(blob.getData()), Charset.defaultCharset()).asSection();
            Assertions.assertEquals(sec, decoded);
        } catch (Exception e) {
            Assertions.fail(e);
        }

        ConfigObject decoded = serializer.deserialize(ConfigContext.INSTANCE, blob).getOrNull();
        Assertions.assertNotNull(decoded);
        Assertions.assertInstanceOf(ConfigSection.class, decoded);

        Assertions.assertEquals(sec, decoded);

    }

}
