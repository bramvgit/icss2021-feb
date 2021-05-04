package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.IfClause;
import nl.han.ica.icss.ast.VariableReference;
import nl.han.ica.icss.ast.literals.BoolLiteral;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IfClauseChecker {
    private final IfClause ifClause;
    private final Map<String, Expression> variables;

    public IfClauseChecker(IfClause ifClause, Map<String, Expression> variables) {
        this.ifClause = ifClause;
        this.variables = variables;
    }

    public void checkConditionalExpression() {
        List<IfClause> invalid = checkIfClauseForInvalidConditionalExpression(ifClause, new ArrayList<>());
        invalid.forEach(clause -> clause.setError(clause.conditionalExpression.toString() + " is an invalid conditional expression."));
    }

    // TODO: doesn't work with undefined variables
    private List<IfClause> checkIfClauseForInvalidConditionalExpression(IfClause ifClause, List<IfClause> invalid) {
        Expression condition = ifClause.conditionalExpression;
        if (condition instanceof VariableReference) {
            condition = variables.get(((VariableReference) condition).name);
        }

        if (!(condition instanceof BoolLiteral)) invalid.add(ifClause);

        for (ASTNode node : ifClause.body) {
            if (node instanceof IfClause) {
                checkIfClauseForInvalidConditionalExpression((IfClause) node, invalid);
            }
        }
        return invalid;
    }
}
