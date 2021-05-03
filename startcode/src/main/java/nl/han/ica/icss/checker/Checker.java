package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;

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

                        // Invalid value for property (for variables)
                        if (declaration.expression instanceof VariableReference) {
                            Expression value = variableTypes.get(((VariableReference) declaration.expression).name);

                            if (declaration.property.name.equalsIgnoreCase("width") && !(value instanceof PixelLiteral)) {
                                checkPropertyValue(declaration, value);
                            }
                        }
                        // Invalid value for property (only hardcoded)
                        else if (declaration.property.name.equalsIgnoreCase("width") && !(declaration.expression instanceof PixelLiteral)) {
                            checkPropertyValue(declaration, declaration.expression);
                        }
                    }
                });
            }
        });
    }

    private void checkPropertyValue(Declaration declaration, Expression value) {
        if (declaration.property.name.equalsIgnoreCase("width")) {
            if (!(value instanceof PixelLiteral) && !(value instanceof PercentageLiteral)) {
                declaration.setError(declaration.property.name + " only accepts pixels or percentage values!");
            }
        }
    }
}
