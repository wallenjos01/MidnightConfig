import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.sql.DatabaseType;
import org.wallentines.mdcfg.sql.SQLConnection;

import java.io.File;

public class TestH2 {

    @Test
    public void testH2() {

        File db = new File("h2");
        SQLConnection conn = DatabaseType.h2(DatabaseType.ResourceType.FILE).create(db.getAbsolutePath());

        Common.testBasics(conn);

    }

}
