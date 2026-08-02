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

    private void scanToken() {
        char currentCharacter = peek();

        switch (currentCharacter) {
            case '+' ->
                    addSingleCharacterToken(TokenType.PLUS);

            case '-' ->
                    addSingleCharacterToken(TokenType.MINUS);

            case '*' ->
                    addSingleCharacterToken(TokenType.STAR);

            case '/' ->
                    addSingleCharacterToken(TokenType.SLASH);

            case '=' ->
                    addSingleCharacterToken(TokenType.EQUAL);

            case '<' ->
                    scanLessThanOperator();

            case '>' ->
                    scanGreaterThanOperator();

            case '.' ->
                    addSingleCharacterToken(TokenType.DOT);

            case '(' ->
                    addSingleCharacterToken(TokenType.LEFT_PAREN);

            case ')' ->
                    addSingleCharacterToken(TokenType.RIGHT_PAREN);

            case '↑' ->
                addSingleCharacterToken(TokenType.LIFT);

            case '↓' ->
                addSingleCharacterToken(TokenType.LOWER);

            case '⊔' ->
                addSingleCharacterToken(TokenType.MERGE);

            case '▷' ->
                addSingleCharacterToken(TokenType.ITERATE);

            case '◁' ->
                addSingleCharacterToken(TokenType.ACCUMULATOR_INIT);

            case '[' ->
                addSingleCharacterToken(TokenType.LEFT_BRACKET);

            case ']' ->
                addSingleCharacterToken(TokenType.RIGHT_BRACKET);

            case '|' ->
                addSingleCharacterToken(TokenType.PIPE);

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
            case "true" ->
                    TokenType.TRUE;

            case "false" ->
                    TokenType.FALSE;

            case "and" ->
                    TokenType.AND;

            case "or" ->
                    TokenType.OR;

            case "xor" ->
                    TokenType.XOR;

            case "implies" ->
                    TokenType.IMPLIES;

            case "not" ->
                    TokenType.NOT;

            case "all" ->
                    TokenType.ALL;

            case "no" ->
                    TokenType.NO;

            case "as" ->
                    TokenType.AS;

            case "Set" ->
                    TokenType.SET;

            case "Bag" ->
                    TokenType.BAG;

            case "OSet" ->
                    TokenType.OSET;

            case "Seq" ->
                    TokenType.SEQ;

            default ->
                    TokenType.IDENTIFIER;
        };

        tokens.add(new Token(
                type,
                lexeme,
                start
        ));
    }

    private void scanLessThanOperator() {
        int position = current;

        // Das Zeichen '<' konsumieren.
        advance();

        if (match('=')) {
            addToken(
                    TokenType.LESS_THAN_OR_EQUAL,
                    "<=",
                    position
            );
            return;
        }

        if (match('>')) {
            addToken(
                    TokenType.NOT_EQUAL,
                    "<>",
                    position
            );
            return;
        }

        addToken(
                TokenType.LESS_THAN,
                "<",
                position
        );
    }

    private void scanGreaterThanOperator() {
        int position = current;

        // Das Zeichen '>' konsumieren.
        advance();

        if (match('=')) {
            addToken(
                    TokenType.GREATER_THAN_OR_EQUAL,
                    ">=",
                    position
            );
            return;
        }

        addToken(
                TokenType.GREATER_THAN,
                ">",
                position
        );
    }

    private void addSingleCharacterToken(TokenType type) {
        int position = current;
        char character = advance();

        addToken(
                type,
                String.valueOf(character),
                position
        );
    }

    private void addToken(
            TokenType type,
            String lexeme,
            int position
    ) {
        tokens.add(new Token(
                type,
                lexeme,
                position
        ));
    }

    private void skipWhitespace() {
        while (!isAtEnd()
                && Character.isWhitespace(peek())) {
            advance();
        }
    }

    private boolean match(char expected) {
        if (isAtEnd()) {
            return false;
        }

        if (peek() != expected) {
            return false;
        }

        advance();
        return true;
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