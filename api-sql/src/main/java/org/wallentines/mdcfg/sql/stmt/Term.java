package org.wallentines.mdcfg.sql.stmt;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Term {

    void write(StatementBuilder builder);

    static StringLiteral literal(String string) {
        return new StringLiteral(string);
    }

    static Array array(List<Term> terms) {
        return new Array(terms);
    }

    static Array array(Stream<Term> terms) {
        return new Array(terms.collect(Collectors.toList()));
    }

    class StringLiteral implements Term {
        private final String value;
        public StringLiteral(String value) {
            this.value = value;
        }

        @Override
        public void write(StatementBuilder builder) {
            builder.append(value);
        }
    }

    class Array implements Term {

        private final List<Term> terms;

        public Array(List<Term> terms) {
            this.terms = terms;
        }

        @Override
        public void write(StatementBuilder builder) {
            for(int i = 0; i < terms.size() ; i++) {
                if(i > 0) {
                    builder.append(",");
                }
                builder.appendTerm(terms.get(i));
            }
        }
    }

}
