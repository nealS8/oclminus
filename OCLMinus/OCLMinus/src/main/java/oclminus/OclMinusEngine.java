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


/**
 * Verarbeitet einen OCL-Minus-Quelltext vollständig, indem er tokenisiert,
 * geparst, typgeprüft und anschließend ausgewertet wird.
 */
public final class OclMinusEngine {

    private final Environment environment; // Enthält Laufzeitwerte von Variablen
    private final ObjectStore objectStore; // Enthält konkrete Objekte eines Modells
    private final TypeEnvironment typeEnvironment; // Enthält Typen von Variablen; wird vom Typchecker benötigt
    private final ModelTypeContext modelTypeContext; // Beschreibt welche Klassen und Eigenschaften existieren (statisch)
    
    // Übergabe von nur einem Argument (Environment); this ruft anderen Konstruktor in der Klasse auf
    public OclMinusEngine(Environment environment) {
        this(environment, new ObjectStore(), new TypeEnvironment(), new ModelTypeContext());
    }

    // Zwei Argumente
    public OclMinusEngine(Environment environment, ObjectStore objectStore) {
        this(environment, objectStore, new TypeEnvironment(), new ModelTypeContext());
    }

    // Drei Argumente
    public OclMinusEngine(Environment environment, ObjectStore objectStore, TypeEnvironment typeEnvironment) {
        this(environment, objectStore, typeEnvironment, new ModelTypeContext());
    }

    // Kein Argument
    public OclMinusEngine() {
        this(new Environment(), new ObjectStore(), new TypeEnvironment(), new ModelTypeContext());
    }

    // Alle Konstruktoren werden hierher geleitet
    public OclMinusEngine(Environment environment, ObjectStore objectStore, TypeEnvironment typeEnvironment,ModelTypeContext modelTypeContext) {
        this.environment = Objects.requireNonNull(environment,
                "Environment darf nicht null sein."
        );

        this.objectStore = Objects.requireNonNull(objectStore,
                "ObjectStore darf nicht null sein."
        );

        this.typeEnvironment = Objects.requireNonNull(typeEnvironment,
                "TypeEnvironment darf nicht null sein."
        );

        this.modelTypeContext = Objects.requireNonNull(modelTypeContext,
                "ModelTypeContext darf nicht null sein."
        );
    }

    public OclValue evaluate(String source) {
        Objects.requireNonNull(source,
                "Source darf nicht null sein."
        );

        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();

        Parser parser = new Parser(tokens);
        Expression expression = parser.parse();

        TypeChecker typeChecker = new TypeChecker(typeEnvironment, modelTypeContext);

        typeChecker.check(expression);

        Interpreter interpreter = new Interpreter(environment, objectStore, typeChecker);

        return interpreter.evaluate(expression);
    }
}