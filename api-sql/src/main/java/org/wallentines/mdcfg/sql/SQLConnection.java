package org.wallentines.mdcfg.sql;

import org.jetbrains.annotations.Nullable;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.sql.stmt.*;

import java.sql.*;
import java.util.*;

public class SQLConnection implements AutoCloseable {

    private final DatabaseType type;
    private final Connection internal;
    private final IDCase idCase;
    private String idQuote;

    public SQLConnection(DatabaseType type, Connection internal) {
        this.type = type;
        this.internal = internal;

        this.idCase = getIDCase();

    }

    public boolean isConnected() {
        try {
            return internal != null && !internal.isClosed();
        } catch (SQLException ex) {
            return false;
        }
    }

    public DatabaseType getType() {
        return type;
    }

    public Connection getInternal() {
        return internal;
    }

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

    public boolean hasTable(String name) {
        return getTables().contains(fixIdentifier(name));
    }

    public CreateTable createTable(String name, TableSchema schema) {
        return new CreateTable(this, name, schema);
    }

    public Select select(String table) {
        return new Select(this, table);
    }

    public Insert insert(String table, Collection<String> columns) {
        return new Insert(this, table, columns);
    }

    public Insert insert(String table, TableSchema schema) {
        return new Insert(this, table, schema.getColumnNames());
    }

    public Update update(String table) {
        return new Update(this, table);
    }

    public Delete delete(String table) {
        return new Delete(this, table);
    }

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
