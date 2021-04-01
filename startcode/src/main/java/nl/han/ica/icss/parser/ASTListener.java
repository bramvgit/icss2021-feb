package nl.han.ica.icss.parser;


import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.HashMap;
import java.util.Map;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {

    //Accumulator attributes:
    private AST ast;

    //Use this to keep track of the parent nodes when recursively traversing the ast
    private IHANStack<ASTNode> currentContainer;

    // Use to keep track of key values for variables
    Map<String, VariableReference> variables;

    public ASTListener() {
        ast = new AST();
        currentContainer = new HANStack<>(10);
        variables = new HashMap<>();
    }

    @Override
    public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
        ParseTree child = getStyleRule(ctx);

        if (isVariableAssignment(child)) {
            addVariableAssignment(child);
        } else {
            // Style rule
            String selector = child.getChild(0).getText();

            addStyleRule();
            addSelector(selector);
            addDeclarations(child);
        }

        if (currentContainer.size() > 0) {
            ast.root.addChild(currentContainer.pop());
        }
    }

    private void addStyleRule() {
        currentContainer.push(new Stylerule());
    }

    private void addVariableAssignment(ParseTree child) {
        VariableAssignment variableAssignment = getVariableAssignment(child);

        currentContainer.push(variableAssignment);
        variables.put(variableAssignment.name.name, variableAssignment.name);
    }

    private ParseTree getStyleRule(ICSSParser.StylesheetContext ctx) {
        if (ctx.getChildCount() > 1) {
            return ctx.getChild(1);
        }
        return ctx.getChild(0);
    }

    private boolean isVariableAssignment(ParseTree child) {
        String assignment = child.getChild(1).getText();
        VariableAssignment variableAssignment = getVariableAssignment(child);

        return Character.isUpperCase(variableAssignment.name.name.charAt(0)) && assignment.equalsIgnoreCase(":=");
    }

    private VariableAssignment getVariableAssignment(ParseTree child) {
        String name = child.getChild(0).getText();
        String value = child.getChild(2).getText();

        VariableAssignment variableAssignment = new VariableAssignment();
        variableAssignment.addChild(new VariableReference(name));
        variableAssignment.addChild(getLiteral(value));

        return variableAssignment;
    }

    private void addDeclarations(ParseTree child) {
        for (int i = 2; i < child.getChildCount() - 1; i++) {
            String property = child.getChild(i).getChild(0).getText();
            ParseTree value = child.getChild(i).getChild(2);

            addDeclaration(property);

            if (!isCalculation(value)) {
                if (isVariableReference(value)) {
                    addVariableReference(value);
                } else {
                    addLiteral(value);
                }
            } else {
                addCalculation(value);
            }
            mergeDeclaration();
        }
    }

    private void addCalculation(ParseTree value) {
        ParseTree startValue = value.getChild(0);
        ParseTree operator = value.getChild(1);
        ParseTree calculation = value.getChild(2);

        // Add operation
        Operation operation = getOperation(operator.getText());

        if (isVariableReference(startValue)) {
            addVariableReferenceToOperation(startValue, operation);
        } else {
            throw new NullPointerException("Not Implemented");
        }

        // Add calculation values
        String val1 = calculation.getChild(0).getChild(0).getText();
        String op = calculation.getChild(0).getChild(1).getText();
        String val2 = calculation.getChild(0).getChild(2).getText();

        Literal l1 = getLiteralWithScalar(val1);
        Literal l2 = getLiteralWithScalar(val2);
        Operation op1 = getOperation(op);

        op1.addChild(l1);
        op1.addChild(l2);

        operation.addChild(op1);
    }

    private void addVariableReferenceToOperation(ParseTree startValue, Operation operation) {
        currentContainer.push(currentContainer.pop()
                .addChild(
                        operation.addChild(
                                variables.get(startValue.getText())
                        )
                )
        );
    }

    private void mergeDeclaration() {
        ASTNode declaration = currentContainer.pop();
        currentContainer.push(
                currentContainer.pop().addChild(declaration)
        );
    }

    private boolean isCalculation(ParseTree value) {
        return value.getChildCount() == 3;
    }

    private void addLiteral(ParseTree value) {
        currentContainer.push(currentContainer.pop()
                .addChild(
                        getLiteral(value.getText())
                )
        );
    }

    private void addDeclaration(String property) {
        currentContainer.push(new Declaration(property));
    }

    private void addVariableReference(ParseTree value) {
        currentContainer.push(currentContainer.pop()
                .addChild(
                        variables.get(
                                value.getText()
                        )
                )
        );
    }

    private boolean isVariableReference(ParseTree value) {
        return Character.isUpperCase(value.getText().charAt(0));
    }

    private Operation getOperation(String operationStr) {
        switch (operationStr) {
            case "+":
                return new AddOperation();
            case "-":
                return new SubtractOperation();
            default:
                return new MultiplyOperation();
        }
    }

    private Literal getLiteral(String value) {
        if (value.endsWith("px")) {
            return new PixelLiteral(value);
        } else if (value.endsWith("%")) {
            return new PercentageLiteral(value);
        } else if (value.startsWith("#")) {
            return new ColorLiteral(value);
        } else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return new BoolLiteral(value);
        }
        return null;
    }

    private Literal getLiteralWithScalar(String value) {
        Literal valueLiteral = null;

        // Get values (color, pixel, literal, etc)
        if (value.endsWith("px")) {
            valueLiteral = new PixelLiteral(value);
        } else if (value.endsWith("%")) {
            valueLiteral = new PercentageLiteral(value);
        } else if (value.startsWith("#")) {
            valueLiteral = new ColorLiteral(value);
        } else if (isNumeric(value)) {
            valueLiteral = new ScalarLiteral(value);
        }

        return valueLiteral;
    }

    private boolean isNumeric(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    private void addSelector(String selector) {
        switch (selector.charAt(0)) {
            case '#':
                currentContainer.push(
                        currentContainer.pop()
                                .addChild(new IdSelector(selector)));
                break;
            case '.':
                currentContainer.push(
                        currentContainer.pop()
                                .addChild(new ClassSelector(selector)));
                break;
            default:
                currentContainer.push(
                        currentContainer.pop()
                                .addChild(new TagSelector(selector)));
        }
    }

    private boolean isStyleRule(ParseTree child) {
        return child.getChildCount() >= 4 && !child.getChild(1).getText().equalsIgnoreCase(":=");
    }

    private boolean isScalar(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public AST getAST() {
        return ast;
    }

}