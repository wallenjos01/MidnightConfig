package org.wallentines.mdcfg.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Java8Util {

    public static <T> List<T> copyList(Collection<T> items) {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

}
