package org.wallentines.mdcfg.codec;

public enum TagType {
    END(0),
    BYTE(1),
    SHORT(2),
    INT(3),
    LONG(4),
    FLOAT(5),
    DOUBLE(6),
    BYTE_ARRAY(7),
    STRING(8),
    LIST(9),
    COMPOUND(10),
    INT_ARRAY(11),
    LONG_ARRAY(12);

    private final byte value;

    TagType(int value) {
        this.value = (byte) value;
    }

    public byte getValue() {
        return value;
    }

    public String encode() {
        return "" + (char) ('a' + value);
    }

    public static TagType byValue(int val) {
        for(TagType t : values()) { if(val == t.value) return t; }
        return null;
    }

    public static TagType parse(String string) {
        if(string != null && string.length() == 1) {
            char c = string.charAt(0);
            return byValue(c - 'a');
        }
        return null;
    }
}
