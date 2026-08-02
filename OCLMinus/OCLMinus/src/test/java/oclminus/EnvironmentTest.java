package oclminus;

import oclminus.runtime.Environment;
import oclminus.runtime.OclInteger;
import oclminus.runtime.OclRelation;
import oclminus.runtime.OclValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

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

@Test
    void childEnvironmentCanAccessParentVariable() {
        Environment parent =
                new Environment();

        OclRelation value =
                new OclRelation(
                        List.of(new OclInteger(1))
                );

        parent.define("value", value);

        Environment child =
                parent.createChild();

        assertEquals(
                value,
                child.lookup("value")
        );
    }

    @Test
    void childEnvironmentShadowsParentVariable() {
        Environment parent =
                new Environment();

        OclRelation parentValue =
                new OclRelation(
                        List.of(new OclInteger(1))
                );

        OclRelation childValue =
                new OclRelation(
                        List.of(new OclInteger(2))
                );

        parent.define("x", parentValue);

        Environment child =
                parent.createChild();

        child.define("x", childValue);

        assertEquals(
                childValue,
                child.lookup("x")
        );

        assertEquals(
                parentValue,
                parent.lookup("x")
        );
    }

    @Test
    void childVariableIsNotVisibleInParent() {
        Environment parent =
                new Environment();

        Environment child =
                parent.createChild();

        child.define(
                "x",
                new OclRelation(
                        List.of(new OclInteger(1))
                )
        );

        assertThrows(
                IllegalStateException.class,
                () -> parent.lookup("x")
        );
    }
}
