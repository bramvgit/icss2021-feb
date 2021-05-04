package nl.han.ica.exception;

import nl.han.ica.icss.ast.IfClause;

public class IfClauseContainsInvalidConditionalExpressionException extends Exception {
    private final IfClause ifClause;

    public IfClauseContainsInvalidConditionalExpressionException(IfClause ifClause) {
        this.ifClause = ifClause;
    }

    public IfClause getIfClause() {
        return ifClause;
    }
}
