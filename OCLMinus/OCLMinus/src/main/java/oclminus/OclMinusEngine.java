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
import oclminus.type.TypeEnvironment;
import oclminus.type.TypeChecker;
public final class OclMinusEngine {

    private final Environment environment;
    private final ObjectStore objectStore;
    private final TypeEnvironment typeEnvironment;

    public OclMinusEngine() {
    this(
            new Environment(),
            new ObjectStore(),
            new TypeEnvironment()
    );
}

public OclMinusEngine(
        Environment environment
) {
    this(
            environment,
            new ObjectStore(),
            new TypeEnvironment()
    );
}

public OclMinusEngine(
        Environment environment,
        ObjectStore objectStore
) {
    this(
            environment,
            objectStore,
            new TypeEnvironment()
    );
}

public OclMinusEngine(
        Environment environment,
        ObjectStore objectStore,
        TypeEnvironment typeEnvironment
) {
    this.environment = Objects.requireNonNull(
            environment,
            "Environment darf nicht null sein."
    );

    this.objectStore = Objects.requireNonNull(
            objectStore,
            "ObjectStore darf nicht null sein."
    );

    this.typeEnvironment = Objects.requireNonNull(
            typeEnvironment,
            "TypeEnvironment darf nicht null sein."
    );
}

    public OclValue evaluate(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();

        Parser parser = new Parser(tokens);
        Expression expression = parser.parse();

        TypeChecker typeChecker =
                new TypeChecker(typeEnvironment);

        typeChecker.check(expression);

        Interpreter interpreter =
                new Interpreter(
                        environment,
                        objectStore,
                        typeChecker
                );

        return interpreter.evaluate(expression);
        }
}
