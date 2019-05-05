package com.thecodesmith.foo;

import java.lang.Thread;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestFoo {
    @Test
    void addition() throws Exception {
        System.out.println("Long-running test...");
        Thread.sleep(600000);
        assertEquals(2, 1 + 1);
    }
}
