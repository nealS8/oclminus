package oclminus.lexer;

import java.util.ArrayList;
import java.util.List;

/**
 * Führt die lexikalische Analyse eines OCL-Minus-Quelltextes durch
 * und erzeugt daraus eine Liste von Tokens.
 */
public final class Lexer {

    private final String source; // Ausdruck welcher ausgewertet werden soll
    private final List<Token> tokens = new ArrayList<>(); // Hier werden die einzelnen Tokens von source gespeichert

    private int current = 0; // Aktuelle Position des Zeichen von source

    public Lexer(String source) {
        if (source == null) {
            throw new IllegalArgumentException(
                    "Source darf nicht null sein."
            );
        }

        this.source = source;
    }

    public List<Token> tokenize() {
        while (!isAtEnd()) { // Solange current < len(source) läuft die Schleife weiter
            scanToken(); // Abhängig vom Typ des Zeichens wird die richtige scan Operation aufgerufen
        }

        tokens.add(new Token(TokenType.EOF, "", current)); // Damit Parser weiß, dass vollständige Eingabe gelesen wurde

        return List.copyOf(tokens); // Liefert unveränderliche Kopie der Tokens; schließt nachträgliche Veränderung aus
    }

    private void scanToken() { // Hier wird entschieden was mit dem aktuellen Zeichen passiert, basierend auf dessen Typ
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

            case '?' ->
                addSingleCharacterToken(TokenType.QUESTION_MARK);

            case ':' ->
                addSingleCharacterToken(TokenType.COLON);

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

        while (!isAtEnd() && Character.isDigit(peek())) { // Schleife wird ausgeführt, solange das Zeichen eine Ziffer ist
            advance(); // Gibt das Zeichen als char zurück und erhöht current++
        }

        String lexeme = source.substring(start, current); // Speichern des (Teil-)Wertes der source als String

        tokens.add(new Token(TokenType.INTEGER, lexeme, start)); // Hinzufügen des gefundenen Tokens zur Liste 
    }

    private void scanWord() { // Wird aufgerufen, wenn das aktuelle Zeichen ein Buchstabe ist
        int start = current;

        while (!isAtEnd()
                && (Character.isLetterOrDigit(peek()) // Liest alle aufeinanderfolgenden Buchstaben ein
                || peek() == '_')) { // Unterstriche z.B. für Identifikatoren erlaubt (Bsp.: first_name)
            advance();
        }

        String lexeme = source.substring(start, current);

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

        tokens.add(new Token(type, lexeme, start));
    }

    private void scanLessThanOperator() {
        int position = current;

        // Das Zeichen '<' konsumieren
        advance(); // current wird erhöht; hat zur Folge, dass das aktuelle Zeichen "konsumiert" wird

        if (match('=')) {
            addToken(TokenType.LESS_THAN_OR_EQUAL, "<=", position);
            return;
        }

        if (match('>')) {
            addToken(TokenType.NOT_EQUAL, "<>", position);
            return;
        }

        addToken(TokenType.LESS_THAN, "<", position);
    }

    private void scanGreaterThanOperator() {
        int position = current;

        // Das Zeichen '>' konsumieren
        advance();

        if (match('=')) {
            addToken(TokenType.GREATER_THAN_OR_EQUAL, ">=", position);
            return;
        }

        addToken(TokenType.GREATER_THAN, ">", position);
    }

    private void addSingleCharacterToken(TokenType type) { // Fügt Einzelzeichen als Token hinzu, bei denen keine weitere Prüfung, 
        int position = current;                            // wie z.B. bei Vergleichsoperatoren nötig ist 
        char character = advance();

        addToken(type, String.valueOf(character), position);
    }

    private void addToken(TokenType type, String lexeme, int position) {
        tokens.add(new Token(type, lexeme, position)); // Fügt Token zur Tokenliste hinzu
    }

    private void skipWhitespace() {
        while (!isAtEnd() && Character.isWhitespace(peek())) {
            advance(); // Überspringt Leerzeichen
        }
    }

    private boolean match(char expected) { // Vergleicht ein übergebenes Zeichen (expected) mit dem aktuellen Zeichen (Position current)
        if (isAtEnd()) {
            return false; 
        }

        if (peek() != expected) {
            return false; // Zeichen sind verschieden
        }

        advance(); // Zeichen sind gleich; current wird erhöht
        return true;
    }

    private char advance() { // "Konsumiert" Zeichen, indem current erhöht wird 
        char result = source.charAt(current);
        current++;
        return result;
    }

    private char peek() { // Gibt das Zeichen der Position current von source zurück
        return source.charAt(current);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

}