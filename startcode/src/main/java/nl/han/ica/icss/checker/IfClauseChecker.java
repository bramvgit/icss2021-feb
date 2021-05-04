package nl.han.ica.icss.checker;

import nl.han.ica.exception.IfClauseContainsInvalidConditionalExpressionException;
import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.IfClause;
import nl.han.ica.icss.ast.VariableReference;
import nl.han.ica.icss.ast.literals.BoolLiteral;

import java.util.Map;

public class IfClauseChecker {
    private final IfClause ifClause;
    private final Map<String, Expression> variables;

    public IfClauseChecker(IfClause ifClause, Map<String, Expression> variables) {
        this.ifClause = ifClause;
        this.variables = variables;
    }

    public void checkConditionalExpression() {
        try {
            getIfClausesWithCorrectConditionalExpression(ifClause);
        } catch (IfClauseContainsInvalidConditionalExpressionException e) {
            ifClause.setError(e.getIfClause().conditionalExpression.getNodeLabel() + " is an invalid conditional expression.");
        }
    }

    // TODO: this doesn't work in parser
    //    Bool := TRUE;
    //    a {
    //        if [Bool] {
    //            width: 10px;
    //            if [Bool] {
    //                width: 10px;
    //            }
    //        }
    //    }
    private IfClause getIfClausesWithCorrectConditionalExpression(IfClause ifClause) throws IfClauseContainsInvalidConditionalExpressionException {
        Expression condition = ifClause.conditionalExpression;
        if (condition instanceof VariableReference) {
            condition = variables.get(((VariableReference) condition).name);
        }

        if (!(condition instanceof BoolLiteral))
            throw new IfClauseContainsInvalidConditionalExpressionException(ifClause);
        return ifClause;
    }
}
