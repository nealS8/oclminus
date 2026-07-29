package oclminus;

import oclminus.runtime.OclBoolean;
import oclminus.runtime.OclInteger;
import oclminus.runtime.OclValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OclMinusEngineTest {

    private final OclMinusEngine engine =
            new OclMinusEngine();

    @Test
    void evaluatesIntegerLiteral() {
        OclValue result = engine.evaluate("42");

        OclInteger integer =
                assertInstanceOf(OclInteger.class, result);

        assertEquals(42, integer.value());
    }

    @Test
    void evaluatesTrueLiteral() {
        OclValue result = engine.evaluate("true");

        OclBoolean bool =
                assertInstanceOf(OclBoolean.class, result);

        assertEquals(true, bool.value());
    }

    @Test
    void evaluatesFalseLiteral() {
        OclValue result = engine.evaluate("false");

        OclBoolean bool =
                assertInstanceOf(OclBoolean.class, result);

        assertEquals(false, bool.value());
    }

    @Test
    void evaluatesAddition() {
        OclValue result = engine.evaluate("2 + 3");

        OclInteger integer =
                assertInstanceOf(OclInteger.class, result);

        assertEquals(5, integer.value());
    }

    @Test
    void evaluatesMultiplication() {
        OclValue result = engine.evaluate("2 * 3");

        OclInteger integer =
                assertInstanceOf(OclInteger.class, result);

        assertEquals(6, integer.value());
    }

    @Test
    void respectsOperatorPrecedence() {
        OclValue result = engine.evaluate("2 + 3 * 4");

        OclInteger integer =
                assertInstanceOf(OclInteger.class, result);

        assertEquals(14, integer.value());
    }

    @Test
    void rejectsBooleanAddition() {
        assertThrows(
                IllegalStateException.class,
                () -> engine.evaluate("true + 3")
        );
    }
}
