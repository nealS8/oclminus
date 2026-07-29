package oclminus.lexer;

import java.util.ArrayList;
import java.util.List;

public final class Lexer {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int current = 0;

    public Lexer(String source) {
        if (source == null) {
            throw new IllegalArgumentException(
                    "Source darf nicht null sein."
            );
        }

        this.source = source;
    }

    public List<Token> tokenize() {
        while (!isAtEnd()) {
            scanToken();
        }

        tokens.add(new Token(
                TokenType.EOF,
                "",
                current
        ));

        return List.copyOf(tokens);
    }

    private void addSingleCharacterToken(TokenType type) {
        int position = current;
        char character = advance();

        tokens.add(new Token(
                type,
                String.valueOf(character),
                position
        ));
    }

    private void scanToken() {
        char currentCharacter = peek();

        switch (currentCharacter) {
            case '+' -> addSingleCharacterToken(TokenType.PLUS);
            case '*' -> addSingleCharacterToken(TokenType.STAR);
            case '.' -> addSingleCharacterToken(TokenType.DOT);

            default -> {
                if (Character.isWhitespace(currentCharacter)) {
                    skipWhitespace();
                    return;
                }

                if (Character.isDigit(currentCharacter)) {
                    scanInteger();
                    return;
                }

                if (Character.isLetter(currentCharacter)) {
                    scanWord();
                    return;
                }

                throw new LexerException(
                        "Ungültiges Zeichen '"
                                + currentCharacter
                                + "' an Position "
                                + current
                );
            }
        }
    }

    private void scanInteger() {
        int start = current;

        while (!isAtEnd()
                && Character.isDigit(peek())) {
            advance();
        }

        String lexeme =
                source.substring(start, current);

        tokens.add(new Token(
                TokenType.INTEGER,
                lexeme,
                start
        ));
    }

    private void scanWord() {
        int start = current;

        while (!isAtEnd()
                && (Character.isLetterOrDigit(peek())
                    || peek() == '_')) {
            advance();
        }

        String lexeme =
                source.substring(start, current);

        TokenType type = switch (lexeme) {
            case "true" -> TokenType.TRUE;
            case "false" -> TokenType.FALSE;
            default -> TokenType.IDENTIFIER;
        };

        tokens.add(new Token(
                type,
                lexeme,
                start
        ));
    }

    private void skipWhitespace() {
        while (!isAtEnd()
                && Character.isWhitespace(peek())) {
            advance();
        }
    }

    private char advance() {
        char result = source.charAt(current);
        current++;
        return result;
    }

    private char peek() {
        return source.charAt(current);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}