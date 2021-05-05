package nl.han.ica.icss.visitor;

import nl.han.ica.icss.ast.*;

import java.util.HashMap;
import java.util.Map;

public class VariableVisitor implements Visitor {
    private final Map<String, ASTNode> variables;
    private final Map<Declaration, ASTNode> declarations;
    private final Map<Operation, Declaration> operations;

    public VariableVisitor() {
        variables = new HashMap<>();
        declarations = new HashMap<>();
        operations = new HashMap<>();
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
            ASTNode variableParent = variables.get(variableReference.name);
            ASTNode declarationParent = declarations.get(declaration);

            checkVariableReferenceScope(variableReference, variableParent, declarationParent);
        } else if (expression instanceof Operation) {
            Operation operation = (Operation) expression;
            operations.put(operation, declaration);
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
        Expression lhs = operation.lhs;
        checkOperationForUndefinedVariables(operation, lhs);

        Expression rhs = operation.rhs;
        checkOperationForUndefinedVariables(operation, rhs);
    }

    private void checkOperationForUndefinedVariables(Operation operation, Expression expression) {
        if (expression instanceof Operation) {
            visit((Operation) expression);
        } else if (expression instanceof VariableReference) {
            ((VariableReference) expression).accept(this);
            VariableReference variableReference = (VariableReference) expression;
            variableReference.accept(this);
            ASTNode variableParent = variables.get(variableReference.name);
            Declaration operationParent = operations.get(operation);
            ASTNode declarationParent = declarations.get(operationParent);

            checkVariableReferenceScope(variableReference, variableParent, declarationParent);
        }
    }

    @Override
    public void visit(IfClause ifClause) {
        ElseClause elseClause = ifClause.elseClause;

        for (ASTNode node : ifClause.body) {
            if (node instanceof VariableAssignment) {
                variables.put(((VariableAssignment) node).name.name, ifClause);
            } else if (node instanceof Declaration) {
                declarations.put((Declaration) node, ifClause);
                ((Declaration) node).accept(this);
            } else if (node instanceof IfClause) {
                ((IfClause) node).accept(this);
            }
        }

        if (ifClause.elseClause != null) {
            for (ASTNode node : elseClause.body) {
                if (node instanceof VariableAssignment) {
                    variables.put(((VariableAssignment) node).name.name, elseClause);
                } else if (node instanceof Declaration) {
                    declarations.put((Declaration) node, elseClause);
                    ((Declaration) node).accept(this);
                }
            }
        }
    }

    @Override
    public void visit(Stylerule stylerule) {
        for (ASTNode node : stylerule.body) {
            if (node instanceof VariableAssignment) {
                variables.put(((VariableAssignment) node).name.name, stylerule);
            } else if (node instanceof Declaration) {
                declarations.put((Declaration) node, stylerule);
                ((Declaration) node).accept(this);
            }
        }
    }

    @Override
    public void visit(Stylesheet stylesheet) {
        for (ASTNode node : stylesheet.body) {
            if (node instanceof VariableAssignment) {
                variables.put(((VariableAssignment) node).name.name, stylesheet);
            }
        }
    }
}
