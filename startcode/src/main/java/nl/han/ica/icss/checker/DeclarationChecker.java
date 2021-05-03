package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.Declaration;
import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.VariableReference;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeclarationChecker {
    private static final Map<String, List<ExpressionType>> DECLARATION_TYPES = configureNotAllowedDeclarationTypes();

    private final Declaration declaration;
    private final Map<String, Expression> variables;

    public DeclarationChecker(Declaration declaration, Map<String, Expression> variables) {
        this.declaration = declaration;
        this.variables = variables;
    }

    public void checkUndefinedVariable() {
        if (declaration.expression == null) {
            declaration.setError(declaration.property + " uses undefined variable reference");
        }
    }

    // TODO: configure more declaration types that aren't allowed
    private static Map<String, List<ExpressionType>> configureNotAllowedDeclarationTypes() {
        Map<String, List<ExpressionType>> declarationTypes = new HashMap<>();

        declarationTypes.put("width", Arrays.asList(
                ExpressionType.BOOL,
                ExpressionType.COLOR
        ));
        declarationTypes.put("height", Arrays.asList(
                ExpressionType.BOOL,
                ExpressionType.COLOR
        ));
        declarationTypes.put("color", Arrays.asList(
                ExpressionType.BOOL,
                ExpressionType.SCALAR,
                ExpressionType.PIXEL,
                ExpressionType.PERCENTAGE
        ));
        return declarationTypes;
    }

    public void checkInvalidVariableType() {
        ExpressionType expressionType = getExpressionType(declaration.expression);
        List<ExpressionType> expressionTypes = DECLARATION_TYPES.get(declaration.property.name);

        if (expressionTypes != null) {
            if (expressionTypes.contains(expressionType)) {
                declaration.setError("Type " + expressionType.name().toLowerCase() + " is not allowed as value on property " + declaration.property.name);
            }
        }
    }

    private ExpressionType getExpressionType(Expression expression) {
        if (expression instanceof VariableReference) {
            expression = variables.get(((VariableReference) expression).name);
        }

        if (expression instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (expression instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        } else if (expression instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        } else if (expression instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        } else if (expression instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        }
        return ExpressionType.UNDEFINED;
    }
}
