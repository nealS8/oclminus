package oclminus;

import java.util.List;
import java.util.Objects;

import oclminus.ast.Expression;
import oclminus.lexer.Lexer;
import oclminus.lexer.Token;
import oclminus.parser.Parser;
import oclminus.runtime.Environment;
import oclminus.runtime.Interpreter;
import oclminus.runtime.ObjectStore;
import oclminus.runtime.OclValue;

public final class OclMinusEngine {

    private final Environment environment;
    private final ObjectStore objectStore;

    public OclMinusEngine() {
    this(
            new Environment(),
            new ObjectStore()
    );
    }

    public OclMinusEngine(Environment environment) {
        this(
                environment,
                new ObjectStore()
        );
    }

    public OclMinusEngine(
            Environment environment,
            ObjectStore objectStore
    ) {
        this.environment = Objects.requireNonNull(
                environment,
                "Environment darf nicht null sein."
        );

        this.objectStore = Objects.requireNonNull(
                objectStore,
                "ObjectStore darf nicht null sein."
        );
    }

    public OclValue evaluate(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();

        Parser parser = new Parser(tokens);
        Expression expression = parser.parse();

        Interpreter interpreter =
        new Interpreter(
                environment,
                objectStore
        );

        return interpreter.evaluate(expression);
    }
}
