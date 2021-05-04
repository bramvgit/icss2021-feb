package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.*;

import java.util.HashMap;
import java.util.Map;


public class Checker {
    private Map<String, Expression> variables;

    public void check(AST ast) {
        variables = new HashMap<>();

        ast.root.body.forEach(part -> {
            // Variable assignment
            if (part instanceof VariableAssignment) {
                VariableAssignment variableAssignment = (VariableAssignment) part;
                variables.put(variableAssignment.name.name, variableAssignment.expression);
            }

            // Style rule
            if (part instanceof Stylerule) {
                Stylerule stylerule = (Stylerule) part;

                stylerule.body.forEach(body -> {

                    if (body instanceof Declaration) {
                        Declaration declaration = (Declaration) body;
                        DeclarationChecker declarationChecker = new DeclarationChecker(declaration, variables);

                        declarationChecker.checkUndefinedVariable();
                        declarationChecker.checkInvalidVariableType();

                        if (declaration.expression instanceof Operation) {
                            OperationChecker operationChecker = new OperationChecker((Operation) declaration.expression, variables);
                            operationChecker.checkPixelAndPercent();
                            operationChecker.checkMultiplyLeftOrRightScalar();
                            operationChecker.checkColorsInOperation();
                            operationChecker.checkUndefinedVariables();
                        }
                    } else if (body instanceof IfClause) {
                        IfClause ifClause = (IfClause) body;
                        IfClauseChecker ifClauseChecker = new IfClauseChecker(ifClause, variables);

                        ifClauseChecker.checkConditionalExpression();
                    }
                });
            }
        });
    }
}
