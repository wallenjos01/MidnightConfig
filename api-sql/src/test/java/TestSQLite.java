import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.sql.SQLConnection;

public class TestSQLite {

    @Test
    public void testSQLite() {

        SQLConnection conn = Common.getDBType("sqlite").create("sqlite/test");

        Common.testBasics(conn);
        Common.testNumberTypes(conn);
        Common.testStringTypes(conn);
        Common.testWhere(conn);

        conn.close();
    }


    @Test
    public void testSQLitePrefixed() {

        SQLConnection conn = Common.getDBType("sqlite").create("sqlite/test", null, null, "pre_", new ConfigSection());

        Common.testBasics(conn);
        Common.testNumberTypes(conn);
        Common.testStringTypes(conn);
        Common.testWhere(conn);

        conn.close();
    }


}
