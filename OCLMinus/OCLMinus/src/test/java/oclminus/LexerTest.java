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

}
