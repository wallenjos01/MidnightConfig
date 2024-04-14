import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.sql.DatabaseType;
import org.wallentines.mdcfg.sql.SQLConnection;

import java.io.File;

public class TestSQLite {

    @Test
    public void testSQLite() {

        SQLConnection conn = Common.getDBType("sqlite").create("test");

        Common.testBasics(conn);
        Common.testNumberTypes(conn);
        Common.testStringTypes(conn);
        Common.testWhere(conn);

        conn.close();
    }

}
