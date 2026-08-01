package oclminus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import oclminus.type.TypeEnvironment;
import oclminus.type.CType;
import oclminus.type.PrimitiveType;
import org.junit.jupiter.api.Test;

final class TypeEnvironmentTest {

    @Test
    void storesAndReturnsVariableType() {
        TypeEnvironment environment =
                new TypeEnvironment();

        CType type =
                CType.singletonOf(
                        PrimitiveType.INTEGER
                );

        environment.define(
                "value",
                type
        );

        assertEquals(
                type,
                environment.lookup("value")
        );

        assertTrue(
                environment.contains("value")
        );
    }

    @Test
    void reportsMissingVariable() {
        TypeEnvironment environment =
                new TypeEnvironment();

        assertFalse(
                environment.contains("missing")
        );

        assertThrows(
                IllegalStateException.class,
                () -> environment.lookup("missing")
        );
    }

    @Test
    void rejectsBlankVariableName() {
        TypeEnvironment environment =
                new TypeEnvironment();

        assertThrows(
                IllegalArgumentException.class,
                () -> environment.define(
                        "",
                        CType.singletonOf(
                                PrimitiveType.INTEGER
                        )
                )
        );
    }

    @Test
    void rejectsNullType() {
        TypeEnvironment environment =
                new TypeEnvironment();

        assertThrows(
                NullPointerException.class,
                () -> environment.define(
                        "value",
                        null
                )
        );
    }
}