package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Evaluator implements Transform {

    private final Map<String, Literal> variables;
    private final List<ASTNode> removedNodes;

    public Evaluator() {
        variables = new HashMap<>();
        removedNodes = new ArrayList<>();
    }

    @Override
    public void apply(AST ast) {
        traverse(ast.root);
    }

    private void traverse(List<ASTNode> nodes) {
        for (ASTNode node : nodes) {
            traverse(node);
        }
    }

    private void traverse(ASTNode node) {
        if (node instanceof Stylesheet) {
            transformStyleSheet((Stylesheet) node);
        } else if (node instanceof Stylerule) {
            transformStylerule((Stylerule) node);
        } else if (node instanceof VariableAssignment) {
            transformVariableAssignment((VariableAssignment) node);
        } else if (node instanceof IfClause) {
            transformIfClause((IfClause) node);
        } else if (node instanceof ElseClause) {
            transformElseClause((ElseClause) node);
        } else if (node instanceof Operation) {
            transformOperation((Operation) node);
        } else if (node instanceof Declaration) {
            transformDeclaration((Declaration) node);
        }
    }

    private void transformDeclaration(Declaration declaration) {
        for (ASTNode node : declaration.getChildren()) {
            if (node instanceof VariableReference) {
                // TODO: replace with literal
            } else {
                traverse(node);
            }
        }
    }

    private void transformElseClause(ElseClause elseClause) {
        traverse(elseClause.getChildren());
        removedNodes.add(elseClause);
    }

    private void transformIfClause(IfClause ifClause) {
        for (ASTNode node : ifClause.getChildren()) {
            if (node instanceof VariableReference) {
                // TODO: replace with literal
            } else {
                traverse(node);
            }
        }
        removedNodes.add(ifClause);
    }

    private void transformStyleSheet(Stylesheet stylesheet) {
        traverse(stylesheet.getChildren());
        for (ASTNode node : removedNodes) {
            stylesheet.removeChild(node);
        }
    }

    private void transformStylerule(Stylerule stylerule) {
//        ArrayList<ASTNode> nodesToKeep = new ArrayList<>();
        traverse(stylerule.getChildren());
//        for (ASTNode node : stylerule.getChildren()) {
//            if (!removedNodes.contains(node)) nodesToKeep.add(node);
//        }
//        stylerule.body = nodesToKeep;
    }

    private void transformVariableAssignment(VariableAssignment variableAssignment) {
        for (ASTNode node : variableAssignment.getChildren()) {
            if (!(node instanceof VariableReference)) {
                traverse(node);
            }
        }

        // TODO: literal throws error when in operation
        variables.put(variableAssignment.name.name, getLiteral(variableAssignment.expression));
        removedNodes.add(variableAssignment);
    }

    private void transformOperation(Operation operation) {
        for (ASTNode node : operation.getChildren()) {
            if (node instanceof VariableReference) {
                // TODO: replace with literal
            } else {
                traverse(node);
            }
        }
    }

    private Literal getLiteral(Expression expression) {
        Literal literal;
        if (expression instanceof PixelLiteral) {
            literal = (PixelLiteral) expression;
        } else if (expression instanceof PercentageLiteral) {
            literal = (PercentageLiteral) expression;
        } else if (expression instanceof ScalarLiteral) {
            literal = (ScalarLiteral) expression;
        } else if (expression instanceof BoolLiteral) {
            literal = (BoolLiteral) expression;
        } else {
            literal = (ColorLiteral) expression;
        }
        return literal;
    }
}
