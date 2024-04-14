import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.sql.SQLConnection;

import java.io.File;

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

}
