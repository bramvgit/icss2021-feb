package nl.han.ica.icss.visitor;

import nl.han.ica.icss.ast.*;

public interface Visitor {
    void visit(VariableAssignment variableAssignment);

    void visit(VariableReference variableReference);

    void visit(Declaration declaration);

    void visit(Operation operation);

    void visit(IfClause ifClause);

    void visit(Stylerule stylerule);

    void visit(Stylesheet stylesheet);
}
