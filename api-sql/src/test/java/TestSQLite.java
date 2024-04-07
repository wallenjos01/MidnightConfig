import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.sql.DatabaseType;
import org.wallentines.mdcfg.sql.SQLConnection;

import java.io.File;

public class TestSQLite {

    @Test
    public void testSQLite() {

        File db = new File("test.db");
        SQLConnection conn = DatabaseType.SQLITE.create(db.getAbsolutePath());

        Common.testBasics(conn);
        Common.testNumberTypes(conn);
        Common.testStringTypes(conn);
        Common.testBlobTypes(conn);
        Common.testWhere(conn);

        conn.close();
    }

}
