package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.checker.visitor.CheckerVisitor;
import nl.han.ica.icss.checker.visitor.Visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Checker {
    private List<Visitor> visitors = new ArrayList<>();

    public void check(AST ast) {
        initializeVisitors();
        ast.root.accept(visitors);

        for (ASTNode node : ast.root.body) {
            if (node instanceof Stylerule) {
                Stylerule stylerule = (Stylerule) node;
                stylerule.accept(visitors);

                for (ASTNode rule : stylerule.body) {
                    if (rule instanceof Declaration) {
                        ((Declaration) rule).accept(visitors);
                    } else if (rule instanceof IfClause) {
                        ((IfClause) rule).accept(visitors);
                    }
                }
            }
        }
    }

    private void initializeVisitors() {
        visitors = Arrays.asList(
                new CheckerVisitor()
        );
    }
}
