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

import java.util.*;

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
        ctx.children.forEach(child -> {
            if (child.getChildCount() == 4) {
                if (child.getChild(1).getText().equalsIgnoreCase(":=")) {
                    String name = child.getChild(0).getText();
                    String value = child.getChild(2).getText();
                    Literal valueLiteral = null;

                    if (value.endsWith("px")) {
                        valueLiteral = new PixelLiteral(value);
                    } else if (value.endsWith("%")) {
                        valueLiteral = new PercentageLiteral(value);
                    } else if (value.startsWith("#")) {
                        valueLiteral = new ColorLiteral(value);
                    } else if (isScalar(value)) {
                        valueLiteral = new ScalarLiteral(value);
                    } else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                        valueLiteral = new BoolLiteral(value);
                    }

                    VariableAssignment assignment = new VariableAssignment();
                    assignment.name = new VariableReference(name);
                    assignment.expression = valueLiteral;

                    variables.put(name, assignment.name);
                    currentContainer.push(assignment);
                }
            }
            if (isStyleRule(child)) {
                addSelector(child);
                addDeclarations(child);
            }
            if (currentContainer.size() > 0) {
                ast.root.addChild(currentContainer.pop());
            }
        });
    }

    private void addDeclarations(ParseTree child) {
        ArrayList<ASTNode> declarations = new ArrayList<>();

        // Declarations start at index 2
        for (int i = 2; i < child.getChildCount(); i++) {
            ParseTree declaration = child.getChild(i);

            // If not end of declarations
            if (!declaration.getText().equals("}")) {
                if (declaration.getChildCount() == 4) {
                    // Get properties
                    String property = declaration.getChild(0).getText();
                    String value = declaration.getChild(2).getText();
                    Literal valueLiteral = getLiteral(value);

                    if (Character.isUpperCase(value.charAt(0)) && declaration.getChild(2).getChildCount() > 1) {
                        String operationStr = declaration.getChild(2).getChild(0).getChild(1).getText();
                        ParseTree calculation = declaration.getChild(2).getChild(1).getChild(0).getChild(0);
                        VariableReference variableReference = variables.get(declaration.getChild(2).getChild(0).getChild(0).getText());

                        Operation operation = getOperation(operationStr);
                        operation.addChild(variableReference);

                        Queue<ASTNode> queue = new LinkedList<>();

                        for (int j = 0; j < calculation.getChildCount(); j++) {
                            String c = calculation.getChild(j).getText();

                            if (c.equalsIgnoreCase("+") || c.equalsIgnoreCase("-") || c.equalsIgnoreCase("*")) {
                                queue.add(getOperation(c));
                            } else {
                                queue.add(getLiteralWithScalar(c));
                            }
                        }

                        Iterator<ASTNode> iterator = queue.iterator();
                        while (iterator.hasNext()) {
                            ASTNode astLiteral1 = iterator.next();
                            ASTNode astOperation = iterator.next();
                            ASTNode astLiteral2 = iterator.next();

                            astOperation.addChild(astLiteral1);
                            astOperation.addChild(astLiteral2);

                            operation.addChild(astOperation);
                        }

                        declarations.add(new Declaration(property).addChild(operation));
                    } else if (valueLiteral != null) {
                        declarations.add(new Declaration(property).addChild(valueLiteral));
                    } else {
                        declarations.add(new Declaration(property).addChild(variables.get(value)));
                    }

                }
            }
        }
        currentContainer.push(new Stylerule((Selector) currentContainer.pop(), declarations));
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
        Literal valueLiteral = null;

        // Get values (color, pixel, literal, etc)
        if (Character.isUpperCase(value.charAt(0))) {
            return valueLiteral;
        }
        if (value.endsWith("px")) {
            valueLiteral = new PixelLiteral(value);
        } else if (value.endsWith("%")) {
            valueLiteral = new PercentageLiteral(value);
        } else if (value.startsWith("#")) {
            valueLiteral = new ColorLiteral(value);
        }

        return valueLiteral;
    }

    private Literal getLiteralWithScalar(String value) {
        Literal valueLiteral = null;

        // Get values (color, pixel, literal, etc)
        if (Character.isUpperCase(value.charAt(0))) {
            return valueLiteral;
        }
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


    private void addSelector(ParseTree child) {
        String selector = child.getChild(0).getText();

        switch (selector.charAt(0)) {
            case '#':
                currentContainer.push(new IdSelector(selector));
                break;
            case '.':
                currentContainer.push(new ClassSelector(selector));
                break;
            default:
                currentContainer.push(new TagSelector(selector));
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