import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.FileCodec;
import org.wallentines.mdcfg.codec.FileCodecRegistry;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class TestFileCodec {

    @Test
    public void testFileCodec() {

        FileCodec json = JSONCodec.fileCodec();
        ConfigObject obj = json.loadFromFile(ConfigContext.INSTANCE, new File("test.json"), StandardCharsets.UTF_8);

        TestJSON.testLoadedFile(obj);
    }

    @Test
    public void testFileWrapper() {

        FileCodec json = JSONCodec.fileCodec();
        FileWrapper<ConfigObject> wrapper = new FileWrapper<>(ConfigContext.INSTANCE, json, new File("test.json"));
        wrapper.load();

        TestJSON.testLoadedFile(wrapper.getRoot());
    }

    @Test
    public void testRegisteredFileCodec() {

        FileCodec json = JSONCodec.fileCodec();
        FileCodecRegistry registry = new FileCodecRegistry();
        
        registry.registerFileCodec(json);

        FileWrapper<ConfigObject> wrapper = registry.fromFile(ConfigContext.INSTANCE, new File("test.json"));
        TestJSON.testLoadedFile(wrapper.getRoot());
        
    }

    @Test
    public void testFindOrCreate() {

        FileCodec json = JSONCodec.fileCodec();
        FileCodecRegistry registry = new FileCodecRegistry();
        registry.registerFileCodec(json);
        Path of = Path.of(".");

        // Existing File
        FileWrapper<ConfigObject> wrapper = registry.findOrCreate(ConfigContext.INSTANCE, "test", of.toFile());
        wrapper.load();
        TestJSON.testLoadedFile(wrapper.getRoot());
        Assertions.assertEquals("test.json", wrapper.getFile().getName());

        // Non-existing File
        File f = new File("test_created.json");
        Assertions.assertTrue(!f.exists() || f.delete());

        wrapper = registry.findOrCreate(ConfigContext.INSTANCE, "test_created", of.toFile());
        Assertions.assertNull(wrapper.getRoot());

        wrapper.setRoot(new ConfigSection().with("Key", "Value"));
        wrapper.save();

        Assertions.assertTrue(f.exists());

        wrapper = registry.findOrCreate(ConfigContext.INSTANCE, "test_defaults", of.toFile(), new ConfigSection().with("key", "value"));
        Assertions.assertNotNull(wrapper.getRoot());
        Assertions.assertTrue(wrapper.getRoot().isSection());
        Assertions.assertEquals("value", wrapper.getRoot().asSection().getString("key"));

        wrapper.save();

        Assertions.assertTrue(f.exists());

    }


}
