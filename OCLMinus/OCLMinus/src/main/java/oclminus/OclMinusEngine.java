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

    // Enthält Laufzeitwerte von Variablen
    private final Environment environment;

    // Enthält konkrete Objekte eines Modells
    private final ObjectStore objectStore;

    // Enthält Typen von Variablen und wird vom TypeChecker benötigt
    private final TypeEnvironment typeEnvironment;

    // Beschreibt statisch, welche Klassen, Vererbungen und Eigenschaften existieren
    private final ModelTypeContext modelTypeContext;


    /**
     * Erstellt eine vollständig leere Standard-Engine.
     *
     * Dabei wird ein gemeinsamer ModelTypeContext erzeugt, der sowohl
     * vom ObjectStore als auch später vom TypeChecker verwendet wird.
     */
    public OclMinusEngine() {
        this(new ModelTypeContext());
    }


    /**
     * Hilfskonstruktor für die Standard-Engine.
     *
     * Wichtig:
     * ObjectStore und Engine erhalten denselben ModelTypeContext.
     */
    private OclMinusEngine(
            ModelTypeContext modelTypeContext
    ) {
        this(
            new Environment(),
            new ObjectStore(modelTypeContext),
            new TypeEnvironment(),
            modelTypeContext
        );
    }


    /**
     * Erstellt eine Engine mit einem bereits vorhandenen Environment.
     *
     * Für ObjectStore und TypeChecker wird ein gemeinsamer neuer
     * ModelTypeContext verwendet.
     */
    public OclMinusEngine(
            Environment environment
    ) {
        this(
            environment,
            new ModelTypeContext()
        );
    }


    /**
     * Hilfskonstruktor für eine Engine mit vorhandenem Environment.
     */
    private OclMinusEngine(
            Environment environment,
            ModelTypeContext modelTypeContext
    ) {
        this(
            environment,
            new ObjectStore(modelTypeContext),
            new TypeEnvironment(),
            modelTypeContext
        );
    }


    /**
     * Vollständiger Konstruktor.
     *
     * Dieser Konstruktor wird verwendet, wenn Environment, ObjectStore,
     * TypeEnvironment und ModelTypeContext bereits außerhalb der Engine
     * aufgebaut wurden.
     *
     * Besonders wichtig ist, dass der übergebene ObjectStore denselben
     * ModelTypeContext verwenden sollte wie der hier übergebene
     * modelTypeContext.
     */
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


    /**
     * Führt einen OCL-Minus-Ausdruck vollständig aus:
     *
     * 1. Lexer
     * 2. Parser
     * 3. TypeChecker
     * 4. Interpreter
     */
    public OclValue evaluate(
            String source
    ) {
        Objects.requireNonNull(
            source,
            "Source darf nicht null sein."
        );

        // ---------------------------------------------------------
        // 1. Lexer
        // ---------------------------------------------------------

        Lexer lexer =
            new Lexer(source);

        List<Token> tokens =
            lexer.tokenize();


        // ---------------------------------------------------------
        // 2. Parser
        // ---------------------------------------------------------

        Parser parser =
            new Parser(tokens);

        Expression expression =
            parser.parse();


        // ---------------------------------------------------------
        // 3. Typprüfung
        // ---------------------------------------------------------

        TypeChecker typeChecker =
            new TypeChecker(
                typeEnvironment,
                modelTypeContext
            );

        typeChecker.check(expression);


        // ---------------------------------------------------------
        // 4. Auswertung
        // ---------------------------------------------------------

        Interpreter interpreter =
            new Interpreter(
                environment,
                objectStore,
                typeChecker
            );

        return interpreter.evaluate(expression);
    }
}