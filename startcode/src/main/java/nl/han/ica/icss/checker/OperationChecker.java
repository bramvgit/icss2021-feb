package nl.han.ica.icss.checker;

import nl.han.ica.exception.OperationContainsPixelAndPercentException;
import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.Operation;
import nl.han.ica.icss.ast.VariableReference;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;

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
}
