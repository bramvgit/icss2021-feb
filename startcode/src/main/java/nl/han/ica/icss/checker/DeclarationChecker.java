package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.Declaration;

public class DeclarationChecker {
    private final Declaration declaration;

    public DeclarationChecker(Declaration declaration) {
        this.declaration = declaration;
    }

    // TODO:
    //    b {
    //        color: 1px + 1px * 1 + Calc;
    //    }
    // Doesn't set error
    public void checkUndefinedVariable() {
        System.out.println(declaration.expression);
        if (declaration.expression == null) {
            declaration.setError(declaration.property + " uses undefined variable reference");
        }
    }
}
