package oclminus;

import oclminus.ast.Expression;
import oclminus.lexer.Lexer;
import oclminus.lexer.Token;
import oclminus.parser.Parser;
import oclminus.runtime.Interpreter;
import oclminus.runtime.OclValue;

import java.util.List;

public final class OclMinusEngine {

    public OclValue evaluate(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();

        Parser parser = new Parser(tokens);
        Expression expression = parser.parse();

        Interpreter interpreter = new Interpreter();
        return interpreter.evaluate(expression);
    }
}
