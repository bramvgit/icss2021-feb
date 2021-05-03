package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.*;

import java.util.HashMap;
import java.util.Map;


public class Checker {

    //private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;
    private Map<String, Expression> variableTypes;

    private Map<String, String> propertyTypes;

    public void check(AST ast) {
        variableTypes = new HashMap<>();
        propertyTypes = new HashMap<>();

        ast.root.body.forEach(part -> {
            // Variable assignment
            if (part instanceof VariableAssignment) {
                VariableAssignment variableAssignment = (VariableAssignment) part;
                variableTypes.put(variableAssignment.name.name, variableAssignment.expression);
            }

            // Style rule
            if (part instanceof Stylerule) {
                Stylerule stylerule = (Stylerule) part;
                stylerule.body.forEach(body -> {
                    if (body instanceof Declaration) {
                        Declaration declaration = (Declaration) body;
                        DeclarationChecker declarationChecker = new DeclarationChecker(declaration);

                        declarationChecker.checkUndefinedVariable();
                    }
                });
            }
        });
    }
}
