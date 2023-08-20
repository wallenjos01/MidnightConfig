package org.wallentines.mdcfg.util;

public class Java8StringUtil {

    public static String repeat(String toRepeat, int count) {

        StringBuilder out = new StringBuilder();
        for(int i = 0 ; i < count ; i++) {
            out.append(toRepeat);
        }

        return out.toString();

    }

}
