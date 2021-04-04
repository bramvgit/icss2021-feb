package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.Declaration;
import nl.han.ica.icss.ast.Stylerule;
import nl.han.ica.icss.ast.VariableAssignment;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;


public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new HANLinkedList<>();
        ast.root.body.forEach(part -> {
            // Variable assignment
            if (part instanceof VariableAssignment) {

            }

            // Style rule
            if (part instanceof Stylerule) {
                Stylerule stylerule = (Stylerule) part;
                stylerule.body.forEach(body -> {
                    if (body instanceof Declaration) {
                        Declaration declaration = (Declaration) body;

                        if (declaration.expression == null) {
                            declaration.setError(declaration.property + " uses undefined variable reference");
                        }
                    }
                });
            }
        });
    }
}
