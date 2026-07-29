package oclminus;

import oclminus.runtime.Environment;
import oclminus.runtime.OclInteger;
import oclminus.runtime.OclValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnvironmentTest {

    @Test
    void storesAndReturnsVariable() {
        Environment environment = new Environment();

        environment.define(
                "x",
                new OclInteger(42)
        );

        OclValue value = environment.lookup("x");

        assertEquals(
                new OclInteger(42),
                value
        );
    }

    @Test
    void rejectsUnknownVariable() {
        Environment environment = new Environment();

        assertThrows(
                IllegalStateException.class,
                () -> environment.lookup("unknown")
        );
    }
}
