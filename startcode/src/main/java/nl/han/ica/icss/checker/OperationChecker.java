package nl.han.ica.icss.checker;

import nl.han.ica.exception.OperationContainsColorException;
import nl.han.ica.exception.OperationContainsPixelAndPercentException;
import nl.han.ica.exception.ScalarNotFoundInMultiplyOperationException;
import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.Operation;
import nl.han.ica.icss.ast.VariableReference;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.MultiplyOperation;

import java.util.Map;

public class OperationChecker {
    private final Operation operation;
    private final Map<String, Expression> variableTypes;
    private boolean pixelLiteral = false;
    private boolean percentageLiteral = false;

    public OperationChecker(Operation operation, Map<String, Expression> variableTypes) {
        this.operation = operation;
        this.variableTypes = variableTypes;
    }

    public void checkPixelAndPercent() {
        try {
            pixelLiteral = false;
            percentageLiteral = false;
            getOperationWithoutPixelAndPercent(operation);
        } catch (OperationContainsPixelAndPercentException e) {
            operation.setError("Calculating with pixels and percentages is not possible.");
        }
    }

    private void getOperationWithoutPixelAndPercent(Operation operation) throws OperationContainsPixelAndPercentException {
        Expression lhs = operation.lhs;
        getOperationWithoutPixelAndPercent(lhs);

        Expression rhs = operation.rhs;
        getOperationWithoutPixelAndPercent(rhs);

        if (pixelLiteral && percentageLiteral) {
            throw new OperationContainsPixelAndPercentException();
        }
    }

    private void getOperationWithoutPixelAndPercent(Expression expression) throws OperationContainsPixelAndPercentException {
        if (expression instanceof VariableReference) {
            VariableReference variableReference = (VariableReference) expression;
            expression = variableTypes.get(variableReference.name);
        }
        if (expression instanceof Operation) {
            getOperationWithoutPixelAndPercent((Operation) expression);
        } else if (expression instanceof PixelLiteral) {
            pixelLiteral = true;
        } else if (expression instanceof PercentageLiteral) {
            percentageLiteral = true;
        }
    }

    public void checkMultiplyLeftOrRightScalar() {
        if (operation instanceof MultiplyOperation) {
            try {
                findScalarInMultiplyOperation(operation);
            } catch (ScalarNotFoundInMultiplyOperationException e) {
                operation.setError("Multiply-calculations require at least 1 scalar.");
            }
        }
    }

    private void findScalarInMultiplyOperation(Operation operation) throws ScalarNotFoundInMultiplyOperationException {
        Expression lhs = operation.lhs;
        if (lhs instanceof MultiplyOperation) {
            findScalarInMultiplyOperation((MultiplyOperation) lhs);
        } else if (lhs instanceof VariableReference) {
            lhs = variableTypes.get(((VariableReference) lhs).name);
        }

        Expression rhs = operation.rhs;
        if (rhs instanceof MultiplyOperation) {
            findScalarInMultiplyOperation((MultiplyOperation) rhs);
        } else if (rhs instanceof VariableReference) {
            lhs = variableTypes.get(((VariableReference) rhs).name);
        }

        if (!(lhs instanceof ScalarLiteral) && !(rhs instanceof ScalarLiteral))
            throw new ScalarNotFoundInMultiplyOperationException();
    }

    public void checkColorsInOperation() {
        try {
            getOperationWithoutColor(operation);
        } catch (OperationContainsColorException e) {
            operation.setError("Calculations cannot contain colors.");
        }
    }

    private void getOperationWithoutColor(Operation operation) throws OperationContainsColorException {
        Expression lhs = operation.lhs;
        getOperationWithoutColor(lhs);

        Expression rhs = operation.rhs;
        getOperationWithoutColor(rhs);
    }

    private void getOperationWithoutColor(Expression expression) throws OperationContainsColorException {
        if (expression instanceof VariableReference) {
            VariableReference variableReference = (VariableReference) expression;
            expression = variableTypes.get(variableReference.name);
        }
        if (expression instanceof Operation) {
            getOperationWithoutColor((Operation) expression);
        } else if (expression instanceof ColorLiteral) {
            throw new OperationContainsColorException();
        }
    }
}
