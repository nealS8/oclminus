package oclminus;

import java.util.List;
import java.util.Objects;

import oclminus.ast.Expression;
import oclminus.lexer.Lexer;
import oclminus.lexer.Token;
import oclminus.parser.Parser;
import oclminus.runtime.Environment;
import oclminus.runtime.Interpreter;
import oclminus.runtime.OclValue;

public final class OclMinusEngine {

    private final Environment environment;

    public OclMinusEngine() {
        this(new Environment());
    }

    public OclMinusEngine(Environment environment) {
        this.environment = Objects.requireNonNull(
                environment,
                "Environment darf nicht null sein."
        );
    }

    public OclValue evaluate(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();

        Parser parser = new Parser(tokens);
        Expression expression = parser.parse();

        Interpreter interpreter =
                new Interpreter(environment);

        return interpreter.evaluate(expression);
    }
}
