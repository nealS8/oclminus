package oclminus;

import oclminus.ast.AllInstancesExpression;
import oclminus.ast.BinaryExpression;
import oclminus.ast.BinaryOperator;
import oclminus.ast.BooleanLiteral;
import oclminus.ast.CoercionExpression;
import oclminus.ast.Expression;
import oclminus.ast.IntegerLiteral;
import oclminus.ast.LiftExpression;
import oclminus.ast.LowerExpression;
import oclminus.ast.NoExpression;
import oclminus.ast.PropertyAccessExpression;
import oclminus.ast.UnaryExpression;
import oclminus.ast.VariableExpression;
import oclminus.lexer.Lexer;
import oclminus.parser.ParseException;
import oclminus.parser.Parser;
import oclminus.type.CollectionKind;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import oclminus.ast.UnaryOperator;

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

@Test
        void parsesMultiplicationBeforeAddition() {
        Expression expression = parse("2 + 3 * 4");

        Expression expected = new BinaryExpression(
                new IntegerLiteral(2),
                BinaryOperator.PLUS,
                new BinaryExpression(
                        new IntegerLiteral(3),
                        BinaryOperator.MULTIPLY,
                        new IntegerLiteral(4)
                )
        );

        assertEquals(expected, expression);
        }

@Test
        void parsesParenthesesBeforeMultiplication() {
        Expression expression = parse("(2 + 3) * 4");

        Expression expected = new BinaryExpression(
                new BinaryExpression(
                        new IntegerLiteral(2),
                        BinaryOperator.PLUS,
                        new IntegerLiteral(3)
                ),
                BinaryOperator.MULTIPLY,
                new IntegerLiteral(4)
        );

        assertEquals(expected, expression);
        }

@Test
        void parsesAdditionBeforeComparison() {
        Expression expression = parse("2 + 3 < 10");

        Expression expected = new BinaryExpression(
                new BinaryExpression(
                        new IntegerLiteral(2),
                        BinaryOperator.PLUS,
                        new IntegerLiteral(3)
                ),
                BinaryOperator.LESS_THAN,
                new IntegerLiteral(10)
        );

        assertEquals(expected, expression);
        }

@Test
        void parsesComparisonsBeforeAnd() {
        Expression expression = parse("2 < 3 and 4 >= 1");

        Expression expected = new BinaryExpression(
                new BinaryExpression(
                        new IntegerLiteral(2),
                        BinaryOperator.LESS_THAN,
                        new IntegerLiteral(3)
                ),
                BinaryOperator.AND,
                new BinaryExpression(
                        new IntegerLiteral(4),
                        BinaryOperator.GREATER_THAN_OR_EQUAL,
                        new IntegerLiteral(1)
                )
        );

        assertEquals(expected, expression);
        }

@Test
        void parsesUnaryNot() {
        Expression expression = parse("not false");

        Expression expected = new UnaryExpression(
                UnaryOperator.NOT,
                new BooleanLiteral(false)
        );

        assertEquals(expected, expression);
        }

@Test
        void parsesUnaryMinus() {
        Expression expression = parse("-5 + 2");

        Expression expected = new BinaryExpression(
                new UnaryExpression(
                        UnaryOperator.NEGATE,
                        new IntegerLiteral(5)
                ),
                BinaryOperator.PLUS,
                new IntegerLiteral(2)
        );

        assertEquals(expected, expression);
        }

@Test
        void parsesAndBeforeOr() {
        Expression expression = parse("true or false and false");

        Expression expected = new BinaryExpression(
                new BooleanLiteral(true),
                BinaryOperator.OR,
                new BinaryExpression(
                        new BooleanLiteral(false),
                        BinaryOperator.AND,
                        new BooleanLiteral(false)
                )
        );

        assertEquals(expected, expression);
        }

@Test
        void parsesOrBeforeImplies() {
        Expression expression = parse("true implies false or true");

        Expression expected = new BinaryExpression(
                new BooleanLiteral(true),
                BinaryOperator.IMPLIES,
                new BinaryExpression(
                        new BooleanLiteral(false),
                        BinaryOperator.OR,
                        new BooleanLiteral(true)
                )
        );

        assertEquals(expected, expression);
        }

@Test
        void parsesAllInstancesExpression() {
        Lexer lexer = new Lexer("all Person");
        Parser parser = new Parser(lexer.tokenize());

        Expression expression = parser.parse();

        assertEquals(
                new AllInstancesExpression("Person"),
                expression
        );
        }

@Test
        void parsesPropertyAccessAfterAllInstances() {
        Lexer lexer = new Lexer("all Person.age");
        Parser parser = new Parser(lexer.tokenize());

        Expression expression = parser.parse();

        assertEquals(
                new PropertyAccessExpression(
                        new AllInstancesExpression("Person"),
                        "age"
                ),
                expression
        );
        }

@Test
        void parsesNoExpression() {
        Lexer lexer = new Lexer("no Person");
        Parser parser = new Parser(lexer.tokenize());

        Expression expression = parser.parse();

        assertEquals(
                new NoExpression("Person"),
                expression
        );
        }

@Test
        void rejectsNoWithoutTypeName() {
        Lexer lexer = new Lexer("no");
        Parser parser = new Parser(lexer.tokenize());

        assertThrows(
                ParseException.class,
                parser::parse
        );
        }

@Test
        void parsesLiftExpression() {
        Expression expression =
                parse("value↑");

        assertEquals(
                new LiftExpression(
                        new VariableExpression("value")
                ),
                expression
        );
        }

@Test
        void parsesLowerExpression() {
        Expression expression =
                parse("value↓");

        assertEquals(
                new LowerExpression(
                        new VariableExpression("value")
                ),
                expression
        );
        }

@Test
        void parsesLiftThenLower() {
        Expression expression =
                parse("value↑↓");

        assertEquals(
                new LowerExpression(
                        new LiftExpression(
                                new VariableExpression("value")
                        )
                ),
                expression
        );
        }

@Test
        void parsesLiftAfterPropertyAccess() {
        Expression expression =
                parse("person.age↑");

        assertEquals(
                new LiftExpression(
                        new PropertyAccessExpression(
                                new VariableExpression("person"),
                                "age"
                        )
                ),
                expression
        );
        }

@Test
        void parsesMergeExpression() {
        Expression expression =
                parse("left ⊔ right");

        assertEquals(
                new BinaryExpression(
                        new VariableExpression("left"),
                        BinaryOperator.MERGE,
                        new VariableExpression("right")
                ),
                expression
        );
        }

@Test
        void parsesMergeLeftAssociatively() {
        Expression expression =
                parse("a ⊔ b ⊔ c");

        assertEquals(
                new BinaryExpression(
                        new BinaryExpression(
                                new VariableExpression("a"),
                                BinaryOperator.MERGE,
                                new VariableExpression("b")
                        ),
                        BinaryOperator.MERGE,
                        new VariableExpression("c")
                ),
                expression
        );
        }

@Test
        void parsesAllCollectionCoercions() {
        assertEquals(
                new CoercionExpression(
                        new VariableExpression("value"),
                        CollectionKind.SET
                ),
                parse("value as Set")
        );

        assertEquals(
                new CoercionExpression(
                        new VariableExpression("value"),
                        CollectionKind.BAG
                ),
                parse("value as Bag")
        );

        assertEquals(
                new CoercionExpression(
                        new VariableExpression("value"),
                        CollectionKind.ORDERED_SET
                ),
                parse("value as OSet")
        );

        assertEquals(
                new CoercionExpression(
                        new VariableExpression("value"),
                        CollectionKind.SEQUENCE
                ),
                parse("value as Seq")
        );
        }

@Test
        void parsesChainedCoercionsLeftAssociatively() {
        assertEquals(
                new CoercionExpression(
                        new CoercionExpression(
                                new VariableExpression("value"),
                                CollectionKind.BAG
                        ),
                        CollectionKind.SET
                ),
                parse("value as Bag as Set")
        );
        }

@Test
        void parsesCoercionAfterLiftAndPropertyAccess() {
        assertEquals(
                new CoercionExpression(
                        new LiftExpression(
                                new PropertyAccessExpression(
                                        new VariableExpression("person"),
                                        "age"
                                )
                        ),
                        CollectionKind.SET
                ),
                parse("person.age↑ as Set")
        );
        }
}
