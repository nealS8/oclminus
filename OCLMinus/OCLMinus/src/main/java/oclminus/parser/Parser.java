package oclminus.parser;

import oclminus.ast.BinaryExpression;
import oclminus.ast.BinaryOperator;
import oclminus.ast.BooleanLiteral;
import oclminus.ast.Expression;
import oclminus.ast.IntegerLiteral;
import oclminus.lexer.Token;
import oclminus.lexer.TokenType;
import oclminus.ast.VariableExpression;
import oclminus.ast.PropertyAccessExpression;
import java.util.List;

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

    private Expression parseExpression() {
        return parseAddition();
    }

    private Expression parseAddition() {
        Expression expression = parseMultiplication();

        while (match(TokenType.PLUS)) {
            Expression right = parseMultiplication();

            expression = new BinaryExpression(
                    expression,
                    BinaryOperator.PLUS,
                    right
            );
        }

        return expression;
    }

    private Expression parseMultiplication() {
        Expression expression = parsePropertyAccess();

        while (match(TokenType.STAR)) {
            Expression right = parsePropertyAccess();

            expression = new BinaryExpression(
                expression,
                BinaryOperator.MULTIPLY,
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

    private boolean match(TokenType expectedType) {
        if (!check(expectedType)) {
            return false;
        }

        advance();
        return true;
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
}