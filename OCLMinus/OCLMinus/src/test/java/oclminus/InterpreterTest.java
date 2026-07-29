package oclminus;

import oclminus.ast.BinaryExpression;
import oclminus.ast.BinaryOperator;
import oclminus.ast.BooleanLiteral;
import oclminus.ast.Expression;
import oclminus.ast.IntegerLiteral;
import oclminus.ast.VariableExpression;
import oclminus.runtime.Environment;
import oclminus.runtime.Interpreter;
import oclminus.runtime.OclBoolean;
import oclminus.runtime.OclInteger;
import oclminus.runtime.OclValue;
import org.junit.jupiter.api.Test;
import oclminus.ast.VariableExpression;
import oclminus.runtime.Environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InterpreterTest {

    private final Interpreter interpreter = new Interpreter();

    @Test
    void evaluatesIntegerLiteral() {

        assertEquals(
                new OclInteger(42),
                interpreter.evaluate(
                        new IntegerLiteral(42)
                )
        );
    }

    @Test
    void evaluatesTrueLiteral() {

        assertEquals(
                new OclBoolean(true),
                interpreter.evaluate(
                        new BooleanLiteral(true)
                )
        );
    }

    @Test
    void evaluatesFalseLiteral() {

        assertEquals(
                new OclBoolean(false),
                interpreter.evaluate(
                        new BooleanLiteral(false)
                )
        );
    }

    @Test
    void evaluatesAddition() {

        Expression expression =
                new BinaryExpression(
                        new IntegerLiteral(2),
                        BinaryOperator.PLUS,
                        new IntegerLiteral(3)
                );

        OclValue result =
                interpreter.evaluate(expression);

        assertEquals(
                new OclInteger(5),
                result
        );
    }

    @Test
    void evaluatesMultiplication() {

        Expression expression =
                new BinaryExpression(
                        new IntegerLiteral(2),
                        BinaryOperator.MULTIPLY,
                        new IntegerLiteral(3)
                );

        OclValue result =
                interpreter.evaluate(expression);

        assertEquals(
                new OclInteger(6),
                result
        );
    }

    @Test
    void evaluatesNestedExpression() {

        Expression expression =
                new BinaryExpression(
                        new IntegerLiteral(2),
                        BinaryOperator.PLUS,
                        new BinaryExpression(
                                new IntegerLiteral(3),
                                BinaryOperator.MULTIPLY,
                                new IntegerLiteral(4)
                        )
                );

        OclValue result =
                interpreter.evaluate(expression);

        assertEquals(
                new OclInteger(14),
                result
        );
    }

    @Test
    void rejectsBooleanAddition() {

        Expression expression =
                new BinaryExpression(
                        new BooleanLiteral(true),
                        BinaryOperator.PLUS,
                        new IntegerLiteral(3)
                );

        assertThrows(
                IllegalStateException.class,
                () -> interpreter.evaluate(expression)
        );
    }

    @Test
    void rejectsNullExpression() {

        assertThrows(
                IllegalArgumentException.class,
                () -> interpreter.evaluate(null)
        );
    }

    @Test
    void evaluatesVariableExpression() {
        Environment environment = new Environment();

        environment.define(
                "x",
                new OclInteger(42)
        );

        Interpreter interpreter =
                new Interpreter(environment);

        OclValue result = interpreter.evaluate(
                new VariableExpression("x")
        );

        assertEquals(
                new OclInteger(42),
                result
        );
    }

    @Test
    void evaluatesExpressionContainingVariable() {
        Environment environment = new Environment();

        environment.define(
                "x",
                new OclInteger(5)
        );

        Interpreter interpreter =
                new Interpreter(environment);

        Expression expression = new BinaryExpression(
                new VariableExpression("x"),
                BinaryOperator.PLUS,
                new IntegerLiteral(3)
        );

        assertEquals(
                new OclInteger(8),
                interpreter.evaluate(expression)
        );
    }
}
