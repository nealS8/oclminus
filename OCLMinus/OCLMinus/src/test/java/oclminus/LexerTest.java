package oclminus;

import oclminus.lexer.Lexer;
import oclminus.lexer.LexerException;
import oclminus.lexer.Token;
import oclminus.lexer.TokenType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LexerTest {

    @Test
    void tokenizesInteger() {
        List<Token> tokens = new Lexer("42").tokenize();

        assertEquals(2, tokens.size());

        assertEquals(TokenType.INTEGER, tokens.get(0).type());
        assertEquals("42", tokens.get(0).lexeme());
        assertEquals(0, tokens.get(0).position());

        assertEquals(TokenType.EOF, tokens.get(1).type());
    }

    @Test
    void tokenizesBooleanLiterals() {
        List<Token> tokens = new Lexer("true false").tokenize();

        assertEquals(TokenType.TRUE, tokens.get(0).type());
        assertEquals(TokenType.FALSE, tokens.get(1).type());
        assertEquals(TokenType.EOF, tokens.get(2).type());
    }

    @Test
    void ignoresWhitespace() {
        List<Token> tokens =
                new Lexer("   42   ").tokenize();

        assertEquals(TokenType.INTEGER, tokens.get(0).type());
        assertEquals(TokenType.EOF, tokens.get(1).type());
    }

    @Test
    void rejectsInvalidCharacter() {
        assertThrows(
                LexerException.class,
                () -> new Lexer("42 €").tokenize()
        );
    }

    @Test
    void tokenizesAdditionAndMultiplication() {
        Lexer lexer = new Lexer("2 + 3 * 4");

        List<Token> tokens = lexer.tokenize();

        assertEquals(
                List.of(
                        new Token(TokenType.INTEGER, "2", 0),
                        new Token(TokenType.PLUS, "+", 2),
                        new Token(TokenType.INTEGER, "3", 4),
                        new Token(TokenType.STAR, "*", 6),
                        new Token(TokenType.INTEGER, "4", 8),
                        new Token(TokenType.EOF, "", 9)
                ),
                tokens
        );
    }

    @Test
    void scansPropertyAccess() {
        Lexer lexer = new Lexer("person.age");

        List<Token> tokens = lexer.tokenize();

        assertEquals(4, tokens.size());

        assertEquals(TokenType.IDENTIFIER, tokens.get(0).type());
        assertEquals("person", tokens.get(0).lexeme());

        assertEquals(TokenType.DOT, tokens.get(1).type());
        assertEquals(".", tokens.get(1).lexeme());

        assertEquals(TokenType.IDENTIFIER, tokens.get(2).type());
        assertEquals("age", tokens.get(2).lexeme());

        assertEquals(TokenType.EOF, tokens.get(3).type());
    }

    @Test
    void scansEqualOperator() {
        Lexer lexer = new Lexer("2 = 2");

        List<Token> tokens = lexer.tokenize();

        assertEquals(TokenType.INTEGER, tokens.get(0).type());
        assertEquals(TokenType.EQUAL, tokens.get(1).type());
        assertEquals(TokenType.INTEGER, tokens.get(2).type());
        assertEquals(TokenType.EOF, tokens.get(3).type());
    }

    @Test
    void tokenizesAllArithmeticOperators() {
        List<Token> tokens =
                new Lexer("1 + 2 - 3 * 4 / 5").tokenize();

        assertEquals(
                List.of(
                        TokenType.INTEGER,
                        TokenType.PLUS,
                        TokenType.INTEGER,
                        TokenType.MINUS,
                        TokenType.INTEGER,
                        TokenType.STAR,
                        TokenType.INTEGER,
                        TokenType.SLASH,
                        TokenType.INTEGER,
                        TokenType.EOF
                ),
                tokens.stream()
                        .map(Token::type)
                        .toList()
        );
    }

    @Test
    void tokenizesAllComparisonOperators() {
        List<Token> tokens =
                new Lexer(
                        "1 = 1 <> 2 < 3 <= 4 > 3 >= 2"
                ).tokenize();

        assertEquals(
                List.of(
                        TokenType.INTEGER,
                        TokenType.EQUAL,
                        TokenType.INTEGER,

                        TokenType.NOT_EQUAL,
                        TokenType.INTEGER,

                        TokenType.LESS_THAN,
                        TokenType.INTEGER,

                        TokenType.LESS_THAN_OR_EQUAL,
                        TokenType.INTEGER,

                        TokenType.GREATER_THAN,
                        TokenType.INTEGER,

                        TokenType.GREATER_THAN_OR_EQUAL,
                        TokenType.INTEGER,

                        TokenType.EOF
                ),
                tokens.stream()
                        .map(Token::type)
                        .toList()
        );
    }

    @Test
    void tokenizesLogicalOperatorsAndParentheses() {
        List<Token> tokens =
                new Lexer(
                        "not (true and false) or true xor false implies true"
                ).tokenize();

        assertEquals(
                List.of(
                        TokenType.NOT,
                        TokenType.LEFT_PAREN,
                        TokenType.TRUE,
                        TokenType.AND,
                        TokenType.FALSE,
                        TokenType.RIGHT_PAREN,
                        TokenType.OR,
                        TokenType.TRUE,
                        TokenType.XOR,
                        TokenType.FALSE,
                        TokenType.IMPLIES,
                        TokenType.TRUE,
                        TokenType.EOF
                ),
                tokens.stream()
                        .map(Token::type)
                        .toList()
        );
    }

}
