import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.ConfigBlob;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class TestConfigBlob {

    @Test
    public void testBasics() {

        Random rand = new Random();
        byte[] data = new byte[1024];
        rand.nextBytes(data);

        ConfigBlob blob = new ConfigBlob(data);

        try(InputStream is = blob.asStream()) {

            int read = 0;
            while(read < data.length) {
                Assertions.assertEquals(data[read], is.read());
                read++;
            }

        } catch(IOException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    public void testCopying() {

        Random rand = new Random();
        byte[] data = new byte[1024];
        rand.nextBytes(data);

        ConfigBlob blob = new ConfigBlob(data);
        ConfigBlob blob2 = blob.copy();

        Assertions.assertEquals(blob, blob2);
        Assertions.assertNotSame(blob, blob2);

    }

}
