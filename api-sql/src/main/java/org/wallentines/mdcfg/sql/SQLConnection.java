package org.wallentines.mdcfg.sql;

import org.jetbrains.annotations.Nullable;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.sql.stmt.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Represents a connection to an SQL database
 */
public class SQLConnection implements AutoCloseable {

    private final DatabaseType type;
    private final Connection internal;
    private final IDCase idCase;
    public final String tablePrefix;
    private String idQuote;

    /**
     * Creates a new connection with the given type and JDBC connection
     * @param type The database type
     * @param internal The JDBC connection
     */
    public SQLConnection(DatabaseType type, Connection internal, @Nullable String tablePrefix) {
        this.type = type;
        this.internal = internal;
        this.tablePrefix = tablePrefix == null ? "" : tablePrefix;

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
            case MIXED_QUOTED:
                return id;
            case LOWER:
            case LOWER_QUOTED:
                return id.toLowerCase();
            case UPPER:
            case UPPER_QUOTED:
                return id.toUpperCase();
        }
    }

    /**
     * Updates the given identifier to match how it is stored in the database
     * @param id The identifier to fix
     * @return The identifier matching how the database would write it
     */
    public String quoteIdentifier(String id) {
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
                String prefix = tablePrefix;
                switch (idCase) {
                    case LOWER:
                        prefix = prefix.toLowerCase();
                        break;
                    case UPPER:
                        prefix = prefix.toUpperCase();
                        break;
                    default:
                        break;
                }

                if(table.startsWith(prefix)) {
                    out.add(table.substring(prefix.length()));
                }
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
        return new CreateTable(this, tablePrefix + name, schema);
    }

    /**
     * Applies the table prefix to the given string
     * @param name The string to apply
     * @return A table identifier with the applied prefix.
     */
    public String applyPrefix(String name) {
        return fixIdentifier(tablePrefix + name);
    }

    /**
     * Creates a column ref with a prefixed table name
     * @param table The name of the table
     * @param column The name of the column
     * @return A new prefixed column ref.
     */
    public ColumnRef column(String table, String column) {
        return new ColumnRef(tablePrefix + table, column);
    }

    /**
     * Starts a SELECT statement
     * @param table The name of the table to select from
     * @return A new SELECT statement
     */
    public Select select(String... table) {
        List<String> tables = new ArrayList<>();
        for(String s : table) {
            tables.add(tablePrefix + s);
        }
        return new Select(this, tables);
    }

    /**
     * Starts a INSERT statement
     * @param table The name of the table to insert into
     * @param schema The schema containing column names to insert values into
     * @return A new INSERT statement
     */
    public Insert insert(String table, TableSchema schema) {
        return new Insert(this, tablePrefix + table, schema, null);
    }

    /**
     * Starts a INSERT statement with columns and a row defined by the given config section
     * @param table The name of the table to insert into
     * @param row The row to insert
     * @return A new INSERT statement
     */
    public Insert insert(String table, ConfigSection row) {
        return new Insert(this, tablePrefix + table, TableSchema.fromSection(row), null).addRow(row);
    }
    /**
     * Starts a INSERT statement
     * @param table The name of the table to insert into
     * @param schema The schema containing column names to insert values into
     * @param returnColumns A list of columns to be put into the UpdateResult generated keys
     * @return A new INSERT statement
     */
    public Insert insert(String table, TableSchema schema, List<String> returnColumns) {
        return new Insert(this, tablePrefix + table, schema, returnColumns);
    }

    /**
     * Starts a INSERT statement with columns and a row defined by the given config section
     * @param table The name of the table to insert into
     * @param row The row to insert
     * @param returnColumns A list of columns to be put into the UpdateResult generated keys
     * @return A new INSERT statement
     */
    public Insert insert(String table, ConfigSection row, List<String> returnColumns) {
        return new Insert(this, tablePrefix + table, TableSchema.fromSection(row), returnColumns).addRow(row);
    }

    /**
     * Starts a UPDATE statement
     * @param table The name of the table to update
     * @param schema The schema of the table to update
     * @return A new UPDATE statement
     */
    public Update update(String table, TableSchema schema) {
        return new Update(this, tablePrefix + table, schema);
    }

    /**
     * Starts a UPDATE statement
     * @param table The name of the table to update
     * @param row The new row values
     * @return A new UPDATE statement
     */
    public Update update(String table, ConfigSection row) {
        return new Update(this, tablePrefix + table, TableSchema.fromSection(row)).withRow(row);
    }

    /**
     * Starts a DELETE statement
     * @param table The name of the table to delete from
     * @return A new DELETE statement
     */
    public Delete delete(String table) {
        return new Delete(this, tablePrefix + table);
    }

    /**
     * Starts a DROP TABLE statement
     * @param table The name of the table to drop
     * @return A new DROP TABLE statement
     */
    public DropTable dropTable(String table) {
        return new DropTable(this, tablePrefix + table);
    }

    /**
     * Starts a CREATE INDEX statement
     * @param name The name of the index to create
     * @param table The name of the table to create the index on
     * @return A new CREATE INDEX statement
     */
    public CreateIndex createIndex(String name, String table) {
        return new CreateIndex(this, name, tablePrefix + table);
    }

    /**
     * Starts a DROP INDEX statement
     * @param name The name of the index to drop
     * @param table The name of the table to drop the index from
     * @return A new DROP INDEX statement
     */
    public DropIndex dropIndex(String name, String table) {
        return new DropIndex(this, name, tablePrefix + table);
    }

    /**
     * Runs the given function on the database without committing any changes made within
     * @param func The function to run. Any statements executed within will not be committed until the function exits and returns true.
     * @return Whether the transaction was successful
     */
    public boolean doTransaction(Function<SQLConnection, Boolean> func) {

        boolean success = false;
        try {
            try {
                internal.setAutoCommit(false);
                if(func.apply(this)) {
                    internal.commit();
                    success = true;
                } else {
                    internal.rollback();
                }
            } finally {
                internal.setAutoCommit(true);
            }
        } catch(SQLException ex) {
            throw new RuntimeException("An exception occurred while running a transaction", ex);
        } 
        return success;
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
