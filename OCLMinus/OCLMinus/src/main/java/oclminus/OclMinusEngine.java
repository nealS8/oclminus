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
import oclminus.type.ModelTypeContext;
import oclminus.type.TypeChecker;
import oclminus.type.TypeEnvironment;

public final class OclMinusEngine {

    private final Environment environment;
    private final ObjectStore objectStore;
    private final TypeEnvironment typeEnvironment;
    private final ModelTypeContext modelTypeContext;

    public OclMinusEngine(
        Environment environment
) {
    this(
            environment,
            new ObjectStore(),
            new TypeEnvironment(),
            new ModelTypeContext()
    );
}

public OclMinusEngine(
        Environment environment,
        ObjectStore objectStore
) {
    this(
            environment,
            objectStore,
            new TypeEnvironment(),
            new ModelTypeContext()
    );
}

public OclMinusEngine(
        Environment environment,
        ObjectStore objectStore,
        TypeEnvironment typeEnvironment
) {
    this(
            environment,
            objectStore,
            typeEnvironment,
            new ModelTypeContext()
    );
}

    public OclMinusEngine() {
        this(
                new Environment(),
                new ObjectStore(),
                new TypeEnvironment(),
                new ModelTypeContext()
        );
    }

    public OclMinusEngine(
            Environment environment,
            ObjectStore objectStore,
            TypeEnvironment typeEnvironment,
            ModelTypeContext modelTypeContext
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

        this.modelTypeContext = Objects.requireNonNull(
                modelTypeContext,
                "ModelTypeContext darf nicht null sein."
        );
    }

    public OclValue evaluate(String source) {
        Objects.requireNonNull(
                source,
                "Source darf nicht null sein."
        );

        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();

        Parser parser = new Parser(tokens);
        Expression expression = parser.parse();

        TypeChecker typeChecker =
                new TypeChecker(
                        typeEnvironment,
                        modelTypeContext
                );

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