import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.TypeReference;

import java.util.List;

public class TestTypeReference {

    @Test
    public void test() {

        TypeReference<List<String>> string = new TypeReference<List<String>>() { };
        Assertions.assertEquals("java.util.List<java.lang.String>", string.getType().toString());
    }

}
