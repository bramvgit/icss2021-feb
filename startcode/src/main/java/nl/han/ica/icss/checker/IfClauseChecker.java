package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.IfClause;

import java.util.Map;

public class IfClauseChecker {
    private final IfClause ifClause;
    private final Map<String, Expression> variables;

    public IfClauseChecker(IfClause ifClause, Map<String, Expression> variables) {
        this.ifClause = ifClause;
        this.variables = variables;
    }


}
