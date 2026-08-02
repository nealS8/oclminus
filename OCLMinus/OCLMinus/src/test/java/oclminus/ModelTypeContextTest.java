package oclminus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import oclminus.type.ModelTypeContext;
import oclminus.type.*;
import org.junit.jupiter.api.Test;

final class ModelTypeContextTest {

    @Test
    void storesAndReturnsPropertyType() {
        ModelTypeContext model =
                new ModelTypeContext();

        CType ageType =
                CType.singletonOf(
                        PrimitiveType.INTEGER
                );

        model.defineProperty(
                "Person",
                "age",
                ageType
        );

        assertEquals(
                ageType,
                model.lookupProperty(
                        "Person",
                        "age"
                )
        );

        assertTrue(
                model.containsProperty(
                        "Person",
                        "age"
                )
        );
    }

    @Test
    void reportsMissingProperty() {
        ModelTypeContext model =
                new ModelTypeContext();

        assertFalse(
                model.containsProperty(
                        "Person",
                        "age"
                )
        );

        assertThrows(
                TypeCheckException.class,
                () -> model.lookupProperty(
                        "Person",
                        "age"
                )
        );
    }

    @Test
    void rejectsBlankClassName() {
        ModelTypeContext model =
                new ModelTypeContext();

        assertThrows(
                IllegalArgumentException.class,
                () -> model.defineProperty(
                        "",
                        "age",
                        CType.singletonOf(
                                PrimitiveType.INTEGER
                        )
                )
        );
    }

    @Test
    void rejectsNullPropertyType() {
        ModelTypeContext model =
                new ModelTypeContext();

        assertThrows(
                NullPointerException.class,
                () -> model.defineProperty(
                        "Person",
                        "age",
                        null
                )
        );
    }
}