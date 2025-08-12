package com.beanbeanjuice.simpleproxychat.test.common;

import com.beanbeanjuice.simpleproxychat.common.Tuple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TupleTest {

    @Test
    @DisplayName("The \"of\" Function Works for Tuples with String-String Values")
    public void testTupleOfFunctionForStringStringValues() {
        Tuple<String, String> testTuple = Tuple.of("StringKey", "StringValue");

        Assertions.assertEquals("StringKey", testTuple.getKey());
        Assertions.assertEquals("StringValue", testTuple.getValue());
    }

    @Test
    @DisplayName("The \"of\" Function Works for Tuples with String-int Values")
    public void testTupleOfFunctionForStringIntValues() {
        Tuple<String, Integer> testTuple = Tuple.of("StringKey", 1);

        Assertions.assertEquals("StringKey", testTuple.getKey());
        Assertions.assertEquals(1, testTuple.getValue());
    }

    @Test
    @DisplayName("Tuples of Any Type Can Be Created")
    public void testTuplesOfAnyTypeCanBeCreated() {
        Tuple<Double, Long> testTuple = new Tuple<>(24.2, 1100L);

        Assertions.assertEquals(24.2, testTuple.getKey());
        Assertions.assertEquals(1100L, testTuple.getValue());
    }

}
