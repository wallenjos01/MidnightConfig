import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.sql.SQLConnection;

import java.io.File;

public class TestH2 {

    @Test
    public void testH2() {

        File db = new File("h2");
        SQLConnection conn = Common.getDBType("h2").create(db.getAbsolutePath());

        Common.testBasics(conn);
        Common.testNumberTypes(conn);
        Common.testStringTypes(conn);
        Common.testBlob(conn);
        Common.testWhere(conn);

        conn.close();
    }

}
