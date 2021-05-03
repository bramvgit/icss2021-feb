package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.Declaration;

public class DeclarationChecker {
    private final Declaration declaration;

    public DeclarationChecker(Declaration declaration) {
        this.declaration = declaration;
    }

    public void checkUndefinedVariable() {
        if (declaration.expression == null) {
            declaration.setError(declaration.property + " uses undefined variable reference");
        }
    }
}
