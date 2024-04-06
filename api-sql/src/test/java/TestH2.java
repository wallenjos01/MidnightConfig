import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.sql.DatabaseType;
import org.wallentines.mdcfg.sql.SQLConnection;

import java.io.File;

public class TestH2 {

    @Test
    public void testH2() {

        File db = new File("test.h2");
        SQLConnection conn = DatabaseType.sqlite().create(db.getAbsolutePath());

        Common.testBasics(conn);

    }

}
