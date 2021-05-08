package nl.han.ica.icss.generator;


import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;

import java.util.List;

public class Generator {
    private StringBuilder stringBuilder;

    public String generate(AST ast) {
        stringBuilder = new StringBuilder();
        traverse(ast.root);
        return stringBuilder.toString();
    }

    private void traverse(List<ASTNode> nodes) {
        for (ASTNode node : nodes) {
            traverse(node);
        }
    }

    private void traverse(ASTNode node) {
        if (node instanceof Stylesheet) {
            generateStyleSheet((Stylesheet) node);
        } else if (node instanceof Stylerule) {
            generateStylerule((Stylerule) node);
        } else if (node instanceof Declaration) {
            generateDeclaration((Declaration) node);
        }
    }

    private void generateStyleSheet(Stylesheet stylesheet) {
        traverse(stylesheet.body);
    }

    private void generateStylerule(Stylerule stylerule) {
        stringBuilder.append(stylerule.selectors.get(0).toString());
        stringBuilder.append(" {\n");

        for (int i = 0; i < stylerule.getChildren().size(); i++) {
            ASTNode node = stylerule.getChildren().get(i);
            if (node instanceof Declaration) {
                traverse(node);
                if (i < stylerule.getChildren().size() - 1) stringBuilder.append('\n');
            }
        }
        stringBuilder.append("\n}");
        stringBuilder.append('\n');
        stringBuilder.append('\n');
    }

    private void generateDeclaration(Declaration declaration) {
        stringBuilder.append(' ');
        stringBuilder.append(' ');
        stringBuilder.append(declaration.property.name);
        stringBuilder.append(": ");
        stringBuilder.append(getValue(declaration.expression));
        stringBuilder.append(';');
    }

    private String getValue(Expression expression) {
        if (expression instanceof PixelLiteral) {
            return ((PixelLiteral) expression).value + "px";
        } else if (expression instanceof PercentageLiteral) {
            return ((PercentageLiteral) expression).value + "%";
        } else if (expression instanceof ColorLiteral) {
            return String.valueOf(((ColorLiteral) expression).value);
        } else {
            return String.valueOf(((ScalarLiteral) expression).value);
        }
    }
}
