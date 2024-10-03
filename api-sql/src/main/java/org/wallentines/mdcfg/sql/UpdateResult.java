package org.wallentines.mdcfg.sql;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.IntStream;

public class UpdateResult {

    private final int[] affectedRows;

    @Nullable
    private final QueryResult generatedKeys;

    public UpdateResult(int[] affectedRows, @Nullable QueryResult generatedKeys) {
        this.affectedRows = affectedRows;
        this.generatedKeys = generatedKeys;
    }

    public boolean hasGeneratedKeys() {
        return generatedKeys != null;
    }

    public int batchCount() {
        return affectedRows.length;
    }

    public int getAffectedRows(int batch) {
        return affectedRows[batch];
    }

    public IntStream getAllAffectedRows() {
        return Arrays.stream(affectedRows);
    }

    @Nullable
    public QueryResult getGeneratedKeys() {
        return generatedKeys;
    }

}
