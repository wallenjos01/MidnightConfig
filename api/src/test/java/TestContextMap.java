import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mdcfg.serializer.ContextMap;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestContextMap {



    @Test
    public void worksWhenEmpty() {

        ContextMap ctx = ContextMap.EMPTY;

        Assertions.assertEquals(0, ctx.values().size());

        Assertions.assertFalse(ctx.getByClass(Object.class).findFirst().isPresent());
        Assertions.assertFalse(ctx.getFirst(Object.class).isPresent());
    }

    @Test
    public void worksWhenSingle() {

        ContextMap ctx = ContextMap.of("Hello");

        Assertions.assertEquals(1, ctx.values().size());

        Assertions.assertTrue(ctx.getByClass(Object.class).findFirst().isPresent());
        Assertions.assertTrue(ctx.getFirst(Object.class).isPresent());

        Assertions.assertTrue(ctx.getByClass(String.class).findFirst().isPresent());
        Assertions.assertTrue(ctx.getFirst(String.class).isPresent());
        Assertions.assertEquals("Hello", ctx.getFirst(String.class).get());
    }

    @Test
    public void worksWhenMultiple() {

        ContextMap ctx = ContextMap.of("Hello", "World");

        Assertions.assertEquals(2, ctx.values().size());

        Assertions.assertTrue(ctx.getByClass(String.class).findFirst().isPresent());
        Assertions.assertEquals(2, ctx.getByClass(String.class).collect(Collectors.toList()).size());
        Assertions.assertTrue(ctx.getFirst(String.class).isPresent());
        Assertions.assertEquals("Hello", ctx.getFirst(String.class).get());
    }

    @Test
    public void worksWhenMultipleDifferentTypes() {

        ContextMap ctx = ContextMap.of("Hello", 3);

        Assertions.assertEquals(2, ctx.values().size());

        Assertions.assertTrue(ctx.getByClass(String.class).findFirst().isPresent());
        Assertions.assertEquals(1, ctx.getByClass(String.class).collect(Collectors.toList()).size());
        Assertions.assertTrue(ctx.getFirst(String.class).isPresent());
        Assertions.assertEquals("Hello", ctx.getFirst(String.class).get());

        Assertions.assertTrue(ctx.getByClass(Integer.class).findFirst().isPresent());
        Assertions.assertEquals(1, ctx.getByClass(Integer.class).collect(Collectors.toList()).size());
        Assertions.assertTrue(ctx.getFirst(Integer.class).isPresent());
        Assertions.assertEquals(3, ctx.getFirst(Integer.class).get());
    }

    @Test
    public void andSingleWorks() {
        ContextMap ctx = ContextMap.of("Hello").and(ContextMap.of("World"));

        Assertions.assertEquals(2, ctx.values().size());

        Assertions.assertTrue(ctx.getByClass(String.class).findFirst().isPresent());
        Assertions.assertEquals(2, ctx.getByClass(String.class).collect(Collectors.toList()).size());
        Assertions.assertTrue(ctx.getFirst(String.class).isPresent());
        Assertions.assertEquals("Hello", ctx.getFirst(String.class).get());
    }

    @Test
    public void andStreamWorks() {

        ContextMap ctx = ContextMap.of("Hello").and(
                Stream.of(ContextMap.of("World"), ContextMap.of(2))
        );

        Assertions.assertEquals(3, ctx.values().size());

        Assertions.assertTrue(ctx.getByClass(String.class).findFirst().isPresent());
        Assertions.assertEquals(2, ctx.getByClass(String.class).collect(Collectors.toList()).size());
        Assertions.assertTrue(ctx.getFirst(String.class).isPresent());
        Assertions.assertEquals("Hello", ctx.getFirst(String.class).get());

        Assertions.assertTrue(ctx.getByClass(Integer.class).findFirst().isPresent());
        Assertions.assertEquals(1, ctx.getByClass(Integer.class).collect(Collectors.toList()).size());
        Assertions.assertTrue(ctx.getFirst(Integer.class).isPresent());
        Assertions.assertEquals(2, ctx.getFirst(Integer.class).get());
    }

}
