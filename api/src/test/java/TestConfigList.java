import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.ConfigList;

public class TestConfigList {

    @Test
    public void testBasics() {

        ConfigList list = new ConfigList();

        Assertions.assertTrue(list.add("Hello"));
        Assertions.assertEquals(1, list.size());
        Assertions.assertTrue(list.get(0).isString());

        list.append(33).add(21);
        Assertions.assertEquals(3, list.size());
        Assertions.assertTrue(list.get(1).isNumber());
        Assertions.assertTrue(list.get(2).isNumber());

        list.remove(2);
        Assertions.assertEquals(2, list.size());
        Assertions.assertTrue(list.get(0).isString());
        Assertions.assertTrue(list.get(1).isNumber());

    }

}
