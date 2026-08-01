package oclminus.parser;

import oclminus.ast.BinaryExpression;
import oclminus.ast.BinaryOperator;
import oclminus.ast.BooleanLiteral;
import oclminus.ast.Expression;
import oclminus.ast.IntegerLiteral;
import oclminus.ast.LiftExpression;
import oclminus.ast.LowerExpression;
import oclminus.lexer.Token;
import oclminus.lexer.TokenType;
import oclminus.ast.VariableExpression;
import oclminus.ast.PropertyAccessExpression;
import oclminus.ast.UnaryExpression;
import oclminus.ast.UnaryOperator;
import oclminus.ast.AllInstancesExpression;
import java.util.List;
import oclminus.ast.NoExpression;

public final class Parser {

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            throw new IllegalArgumentException(
                    "Tokenliste darf nicht null oder leer sein."
            );
        }

        this.tokens = List.copyOf(tokens);
    }

    public Expression parse() {
        Expression expression = parseExpression();

        consume(
                TokenType.EOF,
                "Nach dem Ausdruck wurden unerwartete Tokens gefunden."
        );

        return expression;
    }

    private Expression parseUnary() {
        if (match(TokenType.NOT)) {
            return new UnaryExpression(
                    UnaryOperator.NOT,
                    parseUnary()
            );
        }

        if (match(TokenType.MINUS)) {
            return new UnaryExpression(
                    UnaryOperator.NEGATE,
                    parseUnary()
            );
        }

        return parsePropertyAccess();
    }

    private Expression parseExpression() {
            return parseImplies();
        }

    private Expression parseImplies() {
        Expression expression = parseMerge();

        while (match(TokenType.IMPLIES)) {
            Expression right = parseMerge();

            expression = new BinaryExpression(
                    expression,
                    BinaryOperator.IMPLIES,
                    right
            );
        }

        return expression;
    }

    private Expression parseOr() {
        Expression expression = parseAnd();

        while (match(
                TokenType.OR,
                TokenType.XOR
        )) {

            BinaryOperator operator =
                    switch (previous().type()) {
                        case OR -> BinaryOperator.OR;
                        case XOR -> BinaryOperator.XOR;
                        default -> throw new IllegalStateException();
                    };

            Expression right = parseAnd();

            expression = new BinaryExpression(
                    expression,
                    operator,
                    right
            );
        }

        return expression;
    }

    private Expression parseAnd() {
        Expression expression = parseEquality();

        while (match(TokenType.AND)) {
            Expression right = parseEquality();

            expression = new BinaryExpression(
                    expression,
                    BinaryOperator.AND,
                    right
            );
        }

        return expression;
    }

    private Expression parseEquality() {
        Expression expression = parseComparison();

        while (match(
                TokenType.EQUAL,
                TokenType.NOT_EQUAL
        )) {
            BinaryOperator operator =
                    switch (previous().type()) {
                        case EQUAL ->
                                BinaryOperator.EQUAL;

                        case NOT_EQUAL ->
                                BinaryOperator.NOT_EQUAL;

                        default ->
                                throw new IllegalStateException(
                                        "Unerwarteter Gleichheitsoperator."
                                );
                    };

            Expression right = parseComparison();

            expression = new BinaryExpression(
                    expression,
                    operator,
                    right
            );
        }

        return expression;
    }

    private Expression parseComparison() {
        Expression expression = parseAddition();

        while (match(
                TokenType.LESS_THAN,
                TokenType.LESS_THAN_OR_EQUAL,
                TokenType.GREATER_THAN,
                TokenType.GREATER_THAN_OR_EQUAL
        )) {
            BinaryOperator operator =
                    switch (previous().type()) {
                        case LESS_THAN ->
                                BinaryOperator.LESS_THAN;

                        case LESS_THAN_OR_EQUAL ->
                                BinaryOperator.LESS_THAN_OR_EQUAL;

                        case GREATER_THAN ->
                                BinaryOperator.GREATER_THAN;

                        case GREATER_THAN_OR_EQUAL ->
                                BinaryOperator.GREATER_THAN_OR_EQUAL;

                        default ->
                                throw new IllegalStateException(
                                        "Unerwarteter Vergleichsoperator."
                                );
                    };

            Expression right = parseAddition();

            expression = new BinaryExpression(
                    expression,
                    operator,
                    right
            );
        }

        return expression;
    }

    private Expression parseAddition() {
        Expression expression = parseMultiplication();

        while (match(
                TokenType.PLUS,
                TokenType.MINUS
        )) {

            BinaryOperator operator =
                    switch (previous().type()) {
                        case PLUS -> BinaryOperator.PLUS;
                        case MINUS -> BinaryOperator.MINUS;
                        default -> throw new IllegalStateException();
                    };

            Expression right = parseMultiplication();

            expression = new BinaryExpression(
                    expression,
                    operator,
                    right
            );
        }

        return expression;
    }

    private Expression parseMultiplication() {
        Expression expression = parseUnary();

        while (match(
                TokenType.STAR,
                TokenType.SLASH
        )) {

            BinaryOperator operator =
                    switch (previous().type()) {
                        case STAR -> BinaryOperator.MULTIPLY;
                        case SLASH -> BinaryOperator.DIVIDE;
                        default -> throw new IllegalStateException();
                    };

            Expression right = parseUnary();

            expression = new BinaryExpression(
                    expression,
                    operator,
                    right
            );
        }

        return expression;
    }

    private Expression parsePropertyAccess() {
        Expression expression = parsePrimary();

        while (match(TokenType.DOT)) {
            Token propertyToken = consume(
                    TokenType.IDENTIFIER,
                    "Nach '.' wurde ein Property-Name erwartet."
            );

            expression = new PropertyAccessExpression(
                    expression,
                    propertyToken.lexeme()
            );
        }

        while (true) {

            if (match(TokenType.LIFT)) {
                expression =
                        new LiftExpression(expression);
                continue;
            }

            if (match(TokenType.LOWER)) {
                expression =
                        new LowerExpression(expression);
                continue;
            }

            break;
        }

        return expression;
    }

    private Expression parsePrimary() {
        if (match(TokenType.INTEGER)) {
            Token token = previous();

            try {
                int value = Integer.parseInt(token.lexeme());
                return new IntegerLiteral(value);
            } catch (NumberFormatException exception) {
                throw new ParseException(
                        "Ungültige Ganzzahl '"
                                + token.lexeme()
                                + "' an Position "
                                + token.position()
                );
            }
        }

        if (match(TokenType.TRUE)) {
            return new BooleanLiteral(true);
        }

        if (match(TokenType.FALSE)) {
            return new BooleanLiteral(false);
        }

        if (match(TokenType.ALL)) {
            Token classNameToken = consume(
                    TokenType.IDENTIFIER,
                    "Nach 'all' wurde ein Klassenname erwartet."
            );

            return new AllInstancesExpression(
                    classNameToken.lexeme()
            );
        }

        if (match(TokenType.NO)) {
            Token typeNameToken = consume(
                    TokenType.IDENTIFIER,
                    "Nach 'no' wurde ein Typname erwartet."
            );

            return new NoExpression(
                    typeNameToken.lexeme()
            );
        }

        if (match(TokenType.IDENTIFIER)) {
            Token token = previous();

            return new VariableExpression(
                    token.lexeme()
            );
        }

        Token token = peek();

        throw new ParseException(
                "Grundausdruck erwartet, aber '"
                        + token.lexeme()
                        + "' an Position "
                        + token.position()
                        + " gefunden."
        );
    }

    private boolean match(TokenType... expectedTypes) {
        for (TokenType expectedType : expectedTypes) {
            if (check(expectedType)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(
            TokenType expectedType,
            String errorMessage
    ) {
        if (check(expectedType)) {
            return advance();
        }

        Token token = peek();

        throw new ParseException(
                errorMessage
                        + " Position: "
                        + token.position()
        );
    }

    private boolean check(TokenType expectedType) {
        return peek().type() == expectedType;
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }

        return previous();
    }

    private boolean isAtEnd() {
        return peek().type() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

private Expression parseMerge() {
    Expression expression = parseOr();

    while (match(TokenType.MERGE)) {
        Expression right = parseOr();

        expression = new BinaryExpression(
                expression,
                BinaryOperator.MERGE,
                right
        );
    }

    return expression;
}
}