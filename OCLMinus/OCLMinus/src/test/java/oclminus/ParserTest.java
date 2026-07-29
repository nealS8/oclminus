package oclminus;

import oclminus.ast.BinaryExpression;
import oclminus.ast.BinaryOperator;
import oclminus.ast.BooleanLiteral;
import oclminus.ast.Expression;
import oclminus.ast.IntegerLiteral;
import oclminus.ast.VariableExpression;
import oclminus.lexer.Lexer;
import oclminus.parser.ParseException;
import oclminus.parser.Parser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParserTest {

    @Test
    void parsesIntegerLiteral() {
        assertEquals(
                new IntegerLiteral(42),
                parse("42")
        );
    }

    @Test
    void parsesTrueLiteral() {
        assertEquals(
                new BooleanLiteral(true),
                parse("true")
        );
    }

    @Test
    void parsesFalseLiteral() {
        assertEquals(
                new BooleanLiteral(false),
                parse("false")
        );
    }

    @Test
    void parsesAddition() {

        Expression expected =
                new BinaryExpression(
                        new IntegerLiteral(2),
                        BinaryOperator.PLUS,
                        new IntegerLiteral(3)
                );

        assertEquals(
                expected,
                parse("2 + 3")
        );
    }

    @Test
    void parsesMultiplication() {

        Expression expected =
                new BinaryExpression(
                        new IntegerLiteral(2),
                        BinaryOperator.MULTIPLY,
                        new IntegerLiteral(3)
                );

        assertEquals(
                expected,
                parse("2 * 3")
        );
    }

    @Test
    void multiplicationHasHigherPrecedenceThanAddition() {

        Expression expected =
                new BinaryExpression(
                        new IntegerLiteral(2),
                        BinaryOperator.PLUS,
                        new BinaryExpression(
                                new IntegerLiteral(3),
                                BinaryOperator.MULTIPLY,
                                new IntegerLiteral(4)
                        )
                );

        assertEquals(
                expected,
                parse("2 + 3 * 4")
        );
    }

    @Test
    void additionIsLeftAssociative() {

        Expression expected =
                new BinaryExpression(
                        new BinaryExpression(
                                new IntegerLiteral(2),
                                BinaryOperator.PLUS,
                                new IntegerLiteral(3)
                        ),
                        BinaryOperator.PLUS,
                        new IntegerLiteral(4)
                );

        assertEquals(
                expected,
                parse("2 + 3 + 4")
        );
    }

    @Test
    void multiplicationIsLeftAssociative() {

        Expression expected =
                new BinaryExpression(
                        new BinaryExpression(
                                new IntegerLiteral(2),
                                BinaryOperator.MULTIPLY,
                                new IntegerLiteral(3)
                        ),
                        BinaryOperator.MULTIPLY,
                        new IntegerLiteral(4)
                );

        assertEquals(
                expected,
                parse("2 * 3 * 4")
        );
    }

    @Test
    void rejectsMissingOperandAfterPlus() {
        assertThrows(
                ParseException.class,
                () -> parse("2 +")
        );
    }

    @Test
    void rejectsMissingOperandAfterStar() {
        assertThrows(
                ParseException.class,
                () -> parse("2 *")
        );
    }

    private Expression parse(String source) {

        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer.tokenize());

        return parser.parse();
    }

    @Test
    void parsesVariableExpression() {
    Expression expression = parse("person");

    VariableExpression variable =
            assertInstanceOf(
                    VariableExpression.class,
                    expression
            );

    assertEquals("person", variable.name());
}
}
