package oclminus;

import oclminus.runtime.OclBoolean;
import oclminus.runtime.OclInteger;
import oclminus.runtime.OclRelation;
import oclminus.runtime.OclValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

class OclMinusEngineTest {

    private final OclMinusEngine engine =
            new OclMinusEngine();

    @Test
    void evaluatesIntegerLiteral() {
        OclValue result = engine.evaluate("42");

        OclRelation relation =
                assertInstanceOf(OclRelation.class, result);

        assertEquals(1, relation.elements().size());

        OclInteger integer =
                assertInstanceOf(
                        OclInteger.class,
                        relation.elements().get(0)
                );

        assertEquals(42, integer.value());
    }

    @Test
    void evaluatesTrueLiteral() {
        OclValue result = engine.evaluate("true");

        OclRelation relation =
                assertInstanceOf(OclRelation.class, result);

        assertEquals(1, relation.elements().size());

        OclBoolean bool =
                assertInstanceOf(
                        OclBoolean.class,
                        relation.elements().get(0)
                );

        assertEquals(true, bool.value());
    }

    @Test
    void evaluatesFalseLiteral() {
        OclValue result = engine.evaluate("false");

        OclRelation relation =
                assertInstanceOf(OclRelation.class, result);

        assertEquals(1, relation.elements().size());

        OclBoolean bool =
                assertInstanceOf(
                        OclBoolean.class,
                        relation.elements().get(0)
                );

        assertEquals(false, bool.value());
    }

    @Test
    void evaluatesAddition() {
        OclValue result = engine.evaluate("2 + 3");

        OclRelation relation =
                assertInstanceOf(OclRelation.class, result);

        assertEquals(1, relation.elements().size());

        OclInteger integer =
                assertInstanceOf(
                        OclInteger.class,
                        relation.elements().get(0)
                );

        assertEquals(5, integer.value());
    }

    @Test
    void evaluatesMultiplication() {
        OclValue result = engine.evaluate("2 * 3");

        OclRelation relation =
                assertInstanceOf(OclRelation.class, result);

        assertEquals(1, relation.elements().size());

        OclInteger integer =
                assertInstanceOf(
                        OclInteger.class,
                        relation.elements().get(0)
                );

        assertEquals(6, integer.value());
    }

    @Test
    void respectsOperatorPrecedence() {
        OclValue result = engine.evaluate("2 + 3 * 4");

        OclRelation relation =
                assertInstanceOf(OclRelation.class, result);

        assertEquals(1, relation.elements().size());

        OclInteger integer =
                assertInstanceOf(
                        OclInteger.class,
                        relation.elements().get(0)
                );

        assertEquals(14, integer.value());
    }

    @Test
    void rejectsBooleanAddition() {
        assertThrows(
                IllegalStateException.class,
                () -> engine.evaluate("true + 3")
        );
    }

    @Test
    void evaluatesIntegerEquality() {
        assertEquals(
                new OclRelation(
                        List.of(new OclBoolean(true))
                ),
                engine.evaluate("2 = 2")
        );
    }

    @Test
    void evaluatesIntegerInequality() {
        assertEquals(
                new OclRelation(
                        List.of(new OclBoolean(false))
                ),
                engine.evaluate("2 = 3")
        );
    }

    @Test
    void evaluatesBooleanEquality() {
        assertEquals(
                new OclRelation(
                        List.of(new OclBoolean(true))
                ),
                engine.evaluate("true = true")
        );
    }

    @Test
    void equalityHasLowerPrecedenceThanAddition() {
        assertEquals(
                new OclRelation(
                        List.of(new OclBoolean(true))
                ),
                engine.evaluate("2 + 3 = 5")
        );
    }
}