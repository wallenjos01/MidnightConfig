package org.wallentines.mdcfg;

/**
 * Contains interfaces for functions with up to 15 parameters
 */
public class Functions {

    public interface F1<P1,R> {
        R apply(P1 param1);
    }
    public interface F2<P1,P2,R> {
        R apply(P1 param1, P2 param2);
    }
    public interface F3<P1,P2,P3,R> {
        R apply(P1 param1, P2 param2, P3 param3);
    }
    public interface F4<P1,P2,P3,P4,R> {
        R apply(P1 param1, P2 param2, P3 param3, P4 param4);
    }
    public interface F5<P1,P2,P3,P4,P5,R> {
        R apply(P1 param1, P2 param2, P3 param3, P4 param4, P5 param5);
    }
    public interface F6<P1,P2,P3,P4,P5,P6,R> {
        R apply(P1 param1, P2 param2, P3 param3, P4 param4, P5 param5, P6 param6);
    }
    public interface F7<P1,P2,P3,P4,P5,P6,P7,R> {
        R apply(P1 param1, P2 param2, P3 param3, P4 param4, P5 param5, P6 param6, P7 param7);
    }
    public interface F8<P1,P2,P3,P4,P5,P6,P7,P8,R> {
        R apply(P1 param1, P2 param2, P3 param3, P4 param4, P5 param5, P6 param6, P7 param7, P8 param8);
    }
    public interface F9<P1,P2,P3,P4,P5,P6,P7,P8,P9,R> {
        R apply(P1 param1, P2 param2, P3 param3, P4 param4, P5 param5, P6 param6, P7 param7, P8 param8, P9 param9);
    }
    public interface F10<P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,R> {
        R apply(P1 param1, P2 param2, P3 param3, P4 param4, P5 param5, P6 param6, P7 param7, P8 param8, P9 param9, P10 param10);
    }
    public interface F11<P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,R> {
        R apply(P1 param1, P2 param2, P3 param3, P4 param4, P5 param5, P6 param6, P7 param7, P8 param8, P9 param9, P10 param10, P11 param11);
    }
    public interface F12<P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,R> {
        R apply(P1 param1, P2 param2, P3 param3, P4 param4, P5 param5, P6 param6, P7 param7, P8 param8, P9 param9, P10 param10, P11 param11, P12 param12);
    }
    public interface F13<P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,R> {
        R apply(P1 param1, P2 param2, P3 param3, P4 param4, P5 param5, P6 param6, P7 param7, P8 param8, P9 param9, P10 param10, P11 param11, P12 param12, P13 param13);
    }
    public interface F14<P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,R> {
        R apply(P1 param1, P2 param2, P3 param3, P4 param4, P5 param5, P6 param6, P7 param7, P8 param8, P9 param9, P10 param10, P11 param11, P12 param12, P13 param13, P14 param14);
    }
    public interface F15<P1,P2,P3,P4,P5,P6,P7,P8,P9,P10,P11,P12,P13,P14,P15,R> {
        R apply(P1 param1, P2 param2, P3 param3, P4 param4, P5 param5, P6 param6, P7 param7, P8 param8, P9 param9, P10 param10, P11 param11, P12 param12, P13 param13, P14 param14, P15 param15);
    }

}
