import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.sql.DatabaseType;
import org.wallentines.mdcfg.sql.SQLConnection;

public class TestMariaDB {

    @Test
    public void testMySQL() {

        String url = System.getenv("TEST_MARIADB_URL");
        String user = System.getenv("TEST_MARIADB_USER");
        String pass = System.getenv("TEST_MARIADB_PASS");

        if(url != null && user != null && pass != null) {

            DatabaseType type = Common.getDBType("mariadb");
            try(SQLConnection conn = type.create(url, user, pass)) {
                Common.testAll(conn);
            }

        }

    }

}
