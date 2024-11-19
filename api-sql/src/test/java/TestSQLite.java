import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.sql.SQLConnection;

public class TestSQLite {

    private void testAll(SQLConnection conn) {

        Common.testBasics(conn);
        Common.testUnique(conn);
        Common.testNumberTypes(conn);
        Common.testStringTypes(conn);
        Common.testWhere(conn);
        Common.testJoins(conn);
        Common.testOrderBy(conn);
        Common.testGroupBy(conn);
        Common.testCascade(conn);

    }

    @Test
    public void testSQLite() {

        SQLConnection conn = Common.getDBType("sqlite").create("sqlite/test");
        testAll(conn);
        conn.close();
    }


    @Test
    public void testSQLitePrefixed() {

        SQLConnection conn = Common.getDBType("sqlite").create("sqlite/test", null, null, "pre_", new ConfigSection());
        testAll(conn);
        conn.close();
    }


}
