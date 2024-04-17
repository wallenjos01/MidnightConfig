import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.sql.SQLConnection;

public class TestH2 {

    @Test
    public void testH2() {

        SQLConnection conn = Common.getDBType("h2").create("h2/test");

        Common.testBasics(conn);
        Common.testNumberTypes(conn);
        Common.testStringTypes(conn);
        Common.testBlob(conn);
        Common.testWhere(conn);

        conn.close();
    }

    @Test
    public void testH2Prefixed() {

        SQLConnection conn = Common.getDBType("h2").create("h2/test", null, null, "pre_", new ConfigSection());

        Common.testBasics(conn);
        Common.testNumberTypes(conn);
        Common.testStringTypes(conn);
        Common.testBlob(conn);
        Common.testWhere(conn);

        conn.close();
    }
}
