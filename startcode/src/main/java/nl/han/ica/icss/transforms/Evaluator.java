package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

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
        if (declaration.expression instanceof VariableReference) {
            declaration.expression = variables.get(((VariableReference) declaration.expression).name);
        } else if (declaration.expression instanceof Operation) {
            traverse(declaration.expression);
            declaration.expression = calculate((Operation) declaration.expression);
        }
    }

    private void transformElseClause(ElseClause elseClause) {
        traverse(elseClause.getChildren());
        removedNodes.add(elseClause);
    }

    private void transformIfClause(IfClause ifClause) {
        for (ASTNode node : ifClause.getChildren()) {
            if (node instanceof VariableReference) {
                ifClause.conditionalExpression = variables.get(((VariableReference) node).name);
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
        if (variableAssignment.expression instanceof Operation) {
            traverse(variableAssignment.expression);
            variables.put(variableAssignment.name.name, calculate((Operation) variableAssignment.expression));
        } else {
            variables.put(variableAssignment.name.name, getLiteral(variableAssignment.expression));
        }
        removedNodes.add(variableAssignment);
    }

    private void transformOperation(Operation operation) {
        if (operation.lhs instanceof VariableReference) {
            operation.lhs = variables.get(((VariableReference) operation.lhs).name);
        }
        if (operation.rhs instanceof VariableReference) {
            operation.rhs = variables.get(((VariableReference) operation.rhs).name);
        }
        if (operation.lhs instanceof Operation || operation.rhs instanceof Operation) traverse(operation.getChildren());
    }

    private Literal calculate(Operation operation) {
        if (operation.lhs instanceof Operation) operation.lhs = calculate((Operation) operation.lhs);
        if (operation.rhs instanceof Operation) operation.rhs = calculate((Operation) operation.rhs);

        if (operation.lhs instanceof PixelLiteral || operation.rhs instanceof PixelLiteral) {
            return calculatePixels(operation, (Literal) operation.lhs, (Literal) operation.rhs);
        } else if (operation.lhs instanceof PercentageLiteral || operation.rhs instanceof PercentageLiteral) {
            return calculatePercentages(operation, (Literal) operation.lhs, (Literal) operation.rhs);
        }
        return null;
    }

    private Literal calculatePercentages(Operation operation, Literal lhs, Literal rhs) {
        int n1, n2;
        if (lhs instanceof PercentageLiteral) {
            n1 = ((PercentageLiteral) lhs).value;
        } else {
            n1 = ((ScalarLiteral) lhs).value;
        }

        if (rhs instanceof PercentageLiteral) {
            n2 = ((PercentageLiteral) rhs).value;
        } else {
            n2 = ((ScalarLiteral) rhs).value;
        }

        if (operation instanceof AddOperation) {
            return new PercentageLiteral(Math.addExact(n1, n2));
        } else if (operation instanceof SubtractOperation) {
            return new PercentageLiteral(Math.subtractExact(n1, n2));
        } else {
            return new PercentageLiteral(Math.multiplyExact(n1, n2));
        }
    }

    private Literal calculatePixels(Operation operation, Literal lhs, Literal rhs) {
        int n1, n2;
        if (lhs instanceof PixelLiteral) {
            n1 = ((PixelLiteral) lhs).value;
        } else {
            n1 = ((ScalarLiteral) lhs).value;
        }

        if (rhs instanceof PixelLiteral) {
            n2 = ((PixelLiteral) rhs).value;
        } else {
            n2 = ((ScalarLiteral) rhs).value;
        }

        if (operation instanceof AddOperation) {
            return new PixelLiteral(Math.addExact(n1, n2));
        } else if (operation instanceof SubtractOperation) {
            return new PixelLiteral(Math.subtractExact(n1, n2));
        } else {
            return new PixelLiteral(Math.multiplyExact(n1, n2));
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
