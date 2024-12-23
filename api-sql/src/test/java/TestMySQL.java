import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.sql.DatabaseType;
import org.wallentines.mdcfg.sql.SQLConnection;

public class TestMySQL {

    @Test
    public void testMySQL() {

        String url = System.getenv("TEST_MYSQL_URL");
        String user = System.getenv("TEST_MYSQL_USER");
        String pass = System.getenv("TEST_MYSQL_PASS");

        if(url != null && user != null && pass != null) {

            DatabaseType type = Common.getDBType("mysql");
            try(SQLConnection conn = type.create(url, user, pass)) {
                Common.testAll(conn);
            }
        }

    }

}
