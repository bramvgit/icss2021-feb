package nl.han.ica.icss.visitor;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.*;

public class CheckerVisitor implements Visitor {
    private final Map<String, ASTNode> variableParents;
    private final Map<Declaration, ASTNode> declarationParents;
    private final Map<Operation, Declaration> operationParents;
    private final Map<String, VariableAssignment> variables;
    private final Map<String, List<ExpressionType>> types;
    private final List<String> passedVariables;

    public CheckerVisitor() {
        variableParents = new HashMap<>();
        declarationParents = new HashMap<>();
        operationParents = new HashMap<>();
        variables = new HashMap<>();
        types = new HashMap<>();
        passedVariables = new ArrayList<>();

        initializeTypes();
    }

    private void initializeTypes() {
        List<ExpressionType> colors = Collections.singletonList(
                ExpressionType.COLOR
        );
        List<ExpressionType> width = Arrays.asList(
                ExpressionType.PERCENTAGE,
                ExpressionType.PIXEL,
                ExpressionType.SCALAR
        );
        types.put("color", colors);
        types.put("background-color", colors);
        types.put("width", width);
        types.put("height", width);
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
            VariableAssignment variableAssignment = variables.get(variableReference.name);
            if (variableAssignment == null) {
                expression = null;
            } else {
                expression = variableAssignment.expression;
            }

            variableReference.accept(this);
            ASTNode variableParent = variableParents.get(variableReference.name);
            ASTNode declarationParent = declarationParents.get(declaration);

            checkVariableReferenceScope(variableReference, variableParent, declarationParent);
        } else if (expression instanceof Operation) {
            Operation operation = (Operation) expression;
            operationParents.put(operation, declaration);
            operation.accept(this);
        }
        if (expression != null) {
            List<ExpressionType> expressionType = types.get(declaration.property.name);

            if (expressionType != null) {
                if (expression instanceof PixelLiteral && !expressionType.contains(ExpressionType.PIXEL)) {
                    declaration.setError("Property " + declaration.property.name + " does not accept pixels as value.");
                } else if (expression instanceof PercentageLiteral && !expressionType.contains(ExpressionType.PIXEL)) {
                    declaration.setError("Property " + declaration.property.name + " does not accept percentages as value.");
                } else if (expression instanceof ScalarLiteral && !expressionType.contains(ExpressionType.SCALAR)) {
                    declaration.setError("Property " + declaration.property.name + " does not accept scalars as value.");
                } else if (expression instanceof ColorLiteral && !expressionType.contains(ExpressionType.COLOR)) {
                    declaration.setError("Property " + declaration.property.name + " does not accept colors as value.");
                }
            } else {
                declaration.setError("Property " + declaration.property.name + " does not exist.");
            }
        }
    }

    private void checkVariableReferenceScope(VariableReference variableReference, ASTNode variableParent, ASTNode declarationParent) {
        if (passedVariables.contains(variableReference.name)) {
        } else if (variableParent == null) {
            variableReference.setError(variableReference.name + " is undefined.");
        } else if (!(variableParent instanceof Stylesheet) && !(variableParent.equals(declarationParent))) {
            if (!checkVariableReferenceScope(variableParent, declarationParent)) {
                variableReference.setError(variableReference.name + " is undefined.");
            }
        } else {
            passedVariables.add(variableReference.name);
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
        checkColorsAreNotUsed(operation);

        Expression lhs = operation.lhs;
        checkOperationForUndefinedVariables(operation, lhs);

        Expression rhs = operation.rhs;
        checkOperationForUndefinedVariables(operation, rhs);
    }

    private void checkColorsAreNotUsed(Operation operation) {
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

        if (lhs instanceof ColorLiteral || rhs instanceof ColorLiteral) {
            operation.setError("Colors cannot be used in calculations.");
        }
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
        checkIfClauseUsedBooleanCondition(ifClause);

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

    private void checkIfClauseUsedBooleanCondition(IfClause ifClause) {
        Expression expression = ifClause.conditionalExpression;
        if (expression instanceof VariableReference) {
            VariableAssignment variableAssignment = variables.get(((VariableReference) expression).name);
            if (variableAssignment == null) {
                expression = null;
            } else {
                expression = variableAssignment.expression;
            }
        }
        if (expression != null) {
            if (!(expression instanceof BoolLiteral)) {
                ifClause.setError("If clauses must use a boolean value as conditional expression.");
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
