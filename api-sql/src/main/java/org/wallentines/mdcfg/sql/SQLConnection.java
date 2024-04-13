package org.wallentines.mdcfg.sql;

import org.wallentines.mdcfg.sql.stmt.*;

import java.sql.*;
import java.util.*;

/**
 * Represents a connection to an SQL database
 */
public class SQLConnection implements AutoCloseable {

    private final DatabaseType type;
    private final Connection internal;
    private final IDCase idCase;
    private String idQuote;

    /**
     * Creates a new connection with the given type and JDBC connection
     * @param type The database type
     * @param internal The JDBC connection
     */
    public SQLConnection(DatabaseType type, Connection internal) {
        this.type = type;
        this.internal = internal;

        this.idCase = getIDCase();

    }

    /**
     * Determines if the database is connected
     * @return Whether the database is connected
     */
    public boolean isConnected() {
        try {
            return internal != null && !internal.isClosed();
        } catch (SQLException ex) {
            return false;
        }
    }

    /**
     * Gets the type of the connected database
     * @return The database type
     */
    public DatabaseType getType() {
        return type;
    }

    /**
     * Gets the internal JDBC connection
     * @return The internal JDBC connection
     */
    public Connection getInternal() {
        return internal;
    }

    /**
     * Updates the given identifier to match how it is stored in the database
     * @param id The identifier to fix
     * @return The identifier matching how the database would write it
     */
    public String fixIdentifier(String id) {
        switch (idCase) {
            default:
            case MIXED:
                return id;
            case LOWER:
                return id.toLowerCase();
            case UPPER:
                return id.toUpperCase();
            case MIXED_QUOTED:
                return idQuote + id + idQuote;
            case LOWER_QUOTED:
                return idQuote + id.toLowerCase() + idQuote;
            case UPPER_QUOTED:
                return idQuote + id.toUpperCase() + idQuote;
        }
    }

    private IDCase getIDCase() {
        try {
            DatabaseMetaData meta = internal.getMetaData();
            if(meta.storesMixedCaseIdentifiers()) {
                return IDCase.MIXED;
            } else if (meta.storesUpperCaseIdentifiers()) {
                return IDCase.UPPER;
            } else if (meta.storesLowerCaseIdentifiers()) {
                return IDCase.LOWER;
            } else if(meta.storesMixedCaseQuotedIdentifiers()) {
                idQuote = meta.getIdentifierQuoteString();
                return IDCase.MIXED_QUOTED;
            } else if(meta.storesUpperCaseQuotedIdentifiers()) {
                idQuote = meta.getIdentifierQuoteString();
                return IDCase.UPPER_QUOTED;
            } else if(meta.storesLowerCaseQuotedIdentifiers()) {
                idQuote = meta.getIdentifierQuoteString();
                return IDCase.LOWER_QUOTED;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to determine SQL identifier case!", ex);
        }
        return IDCase.MIXED;
    }

    /**
     * Gets a set of table names in the database
     * @return A set of table names
     */
    public Set<String> getTables() {

        Set<String> out = new HashSet<>();
        try {

            ResultSet set = internal.getMetaData().getTables(null, null, null, null);
            while(set.next()) {
                String table = set.getString("TABLE_NAME");
                out.add(table);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to query tables!", ex);
        }

        return out;
    }

    /**
     * Checks if the given table is in the database
     * @param name The table to check
     * @return Whether the table is in the database
     */
    public boolean hasTable(String name) {
        return getTables().contains(fixIdentifier(name));
    }

    /**
     * Starts a CREATE TABLE statement
     * @param name The name of the table
     * @param schema The table's schema
     * @return A new CREATE TABLE statement
     */
    public CreateTable createTable(String name, TableSchema schema) {
        return new CreateTable(this, name, schema);
    }

    /**
     * Starts a SELECT statement
     * @param table The name of the table to select from
     * @return A new SELECT statement
     */
    public Select select(String table) {
        return new Select(this, table);
    }

    /**
     * Starts a INSERT statement
     * @param table The name of the table to insert into
     * @param columns The columns to insert values into
     * @return A new INSERT statement
     */
    public Insert insert(String table, Collection<String> columns) {
        return new Insert(this, table, columns);
    }

    /**
     * Starts a INSERT statement
     * @param table The name of the table to insert into
     * @param schema The schema containing column names to insert values into
     * @return A new INSERT statement
     */
    public Insert insert(String table, TableSchema schema) {
        return new Insert(this, table, schema.getColumnNames());
    }

    /**
     * Starts a UPDATE statement
     * @param table The name of the table to update
     * @return A new UPDATE statement
     */
    public Update update(String table) {
        return new Update(this, table);
    }

    /**
     * Starts a DELETE statement
     * @param table The name of the table to delete from
     * @return A new DELETE statement
     */
    public Delete delete(String table) {
        return new Delete(this, table);
    }

    /**
     * Starts a DROP TABLE statement
     * @param table The name of the table to drop
     * @return A new DROP TABLE statement
     */
    public DropTable dropTable(String table) {
        return new DropTable(this, table);
    }


    @Override
    public void close() {
        try {
            internal.close();
        } catch (SQLException ex) {
            // Ignore
        }
    }

    private enum IDCase {
        MIXED,
        MIXED_QUOTED,
        LOWER,
        LOWER_QUOTED,
        UPPER,
        UPPER_QUOTED
    }
}
