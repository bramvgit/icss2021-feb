package nl.han.ica.icss.visitor;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.HashMap;
import java.util.Map;

public class VariableVisitor implements Visitor {
    private final Map<String, ASTNode> variableParents;
    private final Map<Declaration, ASTNode> declarationParents;
    private final Map<Operation, Declaration> operationParents;
    private final Map<String, VariableAssignment> variables;

    public VariableVisitor() {
        variableParents = new HashMap<>();
        declarationParents = new HashMap<>();
        operationParents = new HashMap<>();
        variables = new HashMap<>();
    }

    @Override
    public void visit(VariableAssignment variableAssignment) {

    }

    @Override
    public void visit(VariableReference variableReference) {

    }

    @Override
    public void visit(Declaration declaration) {
        Expression expression = declaration.expression;

        if (expression instanceof VariableReference) {
            VariableReference variableReference = (VariableReference) expression;
            variableReference.accept(this);
            ASTNode variableParent = variableParents.get(variableReference.name);
            ASTNode declarationParent = declarationParents.get(declaration);

            checkVariableReferenceScope(variableReference, variableParent, declarationParent);
        } else if (expression instanceof Operation) {
            Operation operation = (Operation) expression;
            operationParents.put(operation, declaration);
            operation.accept(this);
        }
    }

    private void checkVariableReferenceScope(VariableReference variableReference, ASTNode variableParent, ASTNode declarationParent) {
        if (variableParent == null) {
            variableReference.setError(variableReference.name + " is undefined.");
        } else if (!(variableParent instanceof Stylesheet) && !(variableParent.equals(declarationParent))) {
            if (!checkVariableReferenceScope(variableParent, declarationParent)) {
                variableReference.setError(variableReference.name + " is undefined.");
            }
        }
    }

    private boolean checkVariableReferenceScope(ASTNode variableParent, ASTNode declarationParent) {
        if (variableParent.equals(declarationParent)) return true;

        for (ASTNode child : variableParent.getChildren()) {
            boolean correctScope = checkVariableReferenceScope(child, declarationParent);
            if (correctScope) return true;
        }
        return false;
    }

    @Override
    public void visit(Operation operation) {
        checkOperandsTypesAreEqual(operation);

        Expression lhs = operation.lhs;
        checkOperationForUndefinedVariables(operation, lhs);

        Expression rhs = operation.rhs;
        checkOperationForUndefinedVariables(operation, rhs);
    }

    private void checkOperandsTypesAreEqual(Operation operation) {
        Expression lhs = operation.lhs;
        if (lhs instanceof Operation) {
            checkOperandsTypesAreEqual((Operation) lhs);
        }

        Expression rhs = operation.rhs;
        if (rhs instanceof Operation) {
            checkOperandsTypesAreEqual((Operation) rhs);
        }

        if (lhs instanceof VariableReference) {
            lhs = variables.get(((VariableReference) lhs).name).expression;
        }
        if (rhs instanceof VariableReference) {
            rhs = variables.get(((VariableReference) rhs).name).expression;
        }

        if (operation instanceof AddOperation || operation instanceof SubtractOperation) {
            if (lhs instanceof PixelLiteral) {
                if (rhs instanceof PercentageLiteral)
                    operation.setError("Calculating with pixels and percentages is not possible.");
            } else if (lhs instanceof PercentageLiteral) {
                if (rhs instanceof PixelLiteral)
                    operation.setError("Calculating with pixels and percentages is not possible.");
            }
        }

        if (operation instanceof MultiplyOperation) {
            if (!(lhs instanceof ScalarLiteral) && !(rhs instanceof ScalarLiteral)) {
                operation.setError("Multiply operations require at least 1 scalar.");
            }
        }
    }

    private void checkOperationForUndefinedVariables(Operation operation, Expression expression) {
        if (expression instanceof Operation) {
            visit((Operation) expression);
        } else if (expression instanceof VariableReference) {
            ((VariableReference) expression).accept(this);
            VariableReference variableReference = (VariableReference) expression;
            variableReference.accept(this);
            ASTNode variableParent = variableParents.get(variableReference.name);
            Declaration operationParent = operationParents.get(operation);
            ASTNode declarationParent = declarationParents.get(operationParent);

            checkVariableReferenceScope(variableReference, variableParent, declarationParent);
        }
    }

    @Override
    public void visit(IfClause ifClause) {
        ElseClause elseClause = ifClause.elseClause;

        for (ASTNode node : ifClause.body) {
            if (node instanceof VariableAssignment) {
                VariableAssignment variableAssignment = (VariableAssignment) node;

                variables.put(variableAssignment.name.name, variableAssignment);
                variableParents.put(variableAssignment.name.name, ifClause);
            } else if (node instanceof Declaration) {
                declarationParents.put((Declaration) node, ifClause);
                ((Declaration) node).accept(this);
            } else if (node instanceof IfClause) {
                ((IfClause) node).accept(this);
            }
        }

        if (elseClause != null) {
            for (ASTNode node : elseClause.body) {
                if (node instanceof VariableAssignment) {
                    VariableAssignment variableAssignment = (VariableAssignment) node;

                    variables.put(variableAssignment.name.name, variableAssignment);
                    variableParents.put(variableAssignment.name.name, elseClause);
                } else if (node instanceof Declaration) {
                    declarationParents.put((Declaration) node, elseClause);
                    ((Declaration) node).accept(this);
                }
            }
        }
    }

    @Override
    public void visit(Stylerule stylerule) {
        for (ASTNode node : stylerule.body) {
            if (node instanceof VariableAssignment) {
                VariableAssignment variableAssignment = (VariableAssignment) node;

                variables.put(variableAssignment.name.name, variableAssignment);
                variableParents.put(variableAssignment.name.name, stylerule);
            } else if (node instanceof Declaration) {
                declarationParents.put((Declaration) node, stylerule);
                ((Declaration) node).accept(this);
            }
        }
    }

    @Override
    public void visit(Stylesheet stylesheet) {
        for (ASTNode node : stylesheet.body) {
            if (node instanceof VariableAssignment) {
                VariableAssignment variableAssignment = (VariableAssignment) node;

                variables.put(variableAssignment.name.name, variableAssignment);
                variableParents.put(variableAssignment.name.name, stylesheet);
            }
        }
    }
}
