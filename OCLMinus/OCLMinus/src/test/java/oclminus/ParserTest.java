package oclminus;

import oclminus.ast.BinaryExpression;
import oclminus.ast.BinaryOperator;
import oclminus.ast.BooleanLiteral;
import oclminus.ast.Expression;
import oclminus.ast.IntegerLiteral;
import oclminus.ast.PropertyAccessExpression;
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

    @Test
    void parsesPropertyAccess() {
        Expression expression = parse("person.age");

        PropertyAccessExpression propertyAccess =
                assertInstanceOf(
                        PropertyAccessExpression.class,
                        expression
                );

        VariableExpression target =
                assertInstanceOf(
                        VariableExpression.class,
                        propertyAccess.target()
                );

        assertEquals("person", target.name());
        assertEquals("age", propertyAccess.propertyName());
    }

    @Test
    void parsesChainedPropertyAccess() {
        Expression expression = parse("person.address.city");

        PropertyAccessExpression cityAccess =
                assertInstanceOf(
                        PropertyAccessExpression.class,
                        expression
                );

        assertEquals("city", cityAccess.propertyName());

        PropertyAccessExpression addressAccess =
                assertInstanceOf(
                        PropertyAccessExpression.class,
                        cityAccess.target()
                );

        assertEquals("address", addressAccess.propertyName());

        VariableExpression person =
                assertInstanceOf(
                        VariableExpression.class,
                        addressAccess.target()
                );

        assertEquals("person", person.name());
    }

    @Test
    void rejectsMissingPropertyName() {
        assertThrows(
                ParseException.class,
                () -> parse("person.")
        );
    }

@Test
        void parsesEqualityExpression() {
        Expression expression = parse("2 = 3");

        BinaryExpression binary =
                assertInstanceOf(
                        BinaryExpression.class,
                        expression
                );

        assertEquals(BinaryOperator.EQUAL, binary.operator());
        assertEquals(new IntegerLiteral(2), binary.left());
        assertEquals(new IntegerLiteral(3), binary.right());
        }

@Test
        void equalityHasLowerPrecedenceThanAddition() {
        Expression expression = parse("2 + 3 = 5");

        BinaryExpression equality =
                assertInstanceOf(
                        BinaryExpression.class,
                        expression
                );

        assertEquals(BinaryOperator.EQUAL, equality.operator());

        BinaryExpression addition =
                assertInstanceOf(
                        BinaryExpression.class,
                        equality.left()
                );

        assertEquals(BinaryOperator.PLUS, addition.operator());
        }
}
