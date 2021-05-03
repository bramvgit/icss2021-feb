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

import java.util.ArrayList;
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
        ParseTree stylerule = getStyleRule(ctx);

        if (isVariableAssignment(stylerule)) {
            addVariableAssignment(stylerule);
        } else {
            // Style rule
            String selector = stylerule.getChild(0).getText();

            addStyleRule();
            addSelector(selector);

            // Loop through all bodies
            ParseTree body = stylerule.getChild(2);
            if (body != null) {
                for (int i = 0; i < body.getChildCount(); i++) {
                    if (isIfClause(body.getChild(i))) {
                        currentContainer.push(currentContainer.pop().addChild(getIfClause(
                                body.getChild(i),
                                null)
                        ));
                    } else if (isIfElseClause(body.getChild(i))) {
                        currentContainer.push(currentContainer.pop().addChild(getIfClause(
                                body.getChild(i),
                                new ElseClause(getIfClauseBody(body.getChild(i).getChild(2))))
                        ));
                    } else {
                        addDeclaration(body.getChild(i));
                        mergeDeclaration();
                    }
                }
            }
        }

        if (currentContainer.size() > 0) {
            ast.root.addChild(currentContainer.pop());
        }
    }

    private IfClause getIfClause(ParseTree tree, ElseClause elseClause) {
        IfClause ifClause = new IfClause(
                getExpression(tree),
                getIfClauseBody(tree.getChild(5)),
                elseClause
        );

        ParseTree body = tree.getChild(5);

        for (int i = 0; i < body.getChildCount(); i++) {
            if (isIfClause(body.getChild(i))) {
                ifClause.body.add(getIfClause(
                        body.getChild(i).getChild(0),
                        null
                ));
            } else if (isIfElseClause(body.getChild(i))) {
                ifClause.body.add(getIfClause(
                        body.getChild(i).getChild(0),
                        new ElseClause(getIfClauseBody(body.getChild(i).getChild(1).getChild(2)))
                ));
            }
        }
        return ifClause;
    }

    private ArrayList<ASTNode> getIfClauseBody(ParseTree ifClauseBody) {
        ArrayList<ASTNode> declarations = new ArrayList<>();

        for (int i = 0; i < ifClauseBody.getChildCount(); i++) {
            if (isDeclaration(ifClauseBody.getChild(i))) {
                addDeclaration(ifClauseBody.getChild(i));
                declarations.add(currentContainer.pop());
            }
        }
        return declarations;
    }

    private boolean isDeclaration(ParseTree child) {
        return child.getChildCount() == 4;
    }

    private boolean isIfElseClause(ParseTree child) {
        return child.getChildCount() == 2;
    }

    private Expression getExpression(ParseTree child) {
        return variables.get(child.getChild(2).getText());
    }

    private boolean isIfClause(ParseTree body) {
        return body.getChildCount() == 7;
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
        return Character.isUpperCase(
                child.getChild(0).getText().charAt(0))
                && child.getChildCount() == 4
                && child.getChild(1).getText().equalsIgnoreCase(":="
        );
    }

    private VariableAssignment getVariableAssignment(ParseTree child) {
        VariableAssignment variableAssignment = new VariableAssignment();
        variableAssignment.name = new VariableReference(
                child.getChild(0).getText()
        );
        ParseTree value = child.getChild(2);

        // If assignment contains a calculation
        if (value.getChild(0).getChildCount() == 1) {
            if (value.getChild(0).getChild(0).getChildCount() == 1) {
                if (value.getChild(0).getChild(0).getChild(0).getChildCount() == 3) {
                    ParseTree calculation = value.getChild(0).getChild(0).getChild(0);

                    if (isCalculation(calculation)) {
                        variableAssignment.expression = getVariableAssignmentOperation(calculation);
                        return variableAssignment;
                    }
                }
            }
        }

        variableAssignment.addChild(getLiteral(value.getText()));
        return variableAssignment;
    }

    private Operation getVariableAssignmentOperation(ParseTree calculation) {
        Operation operation = getOperation(calculation.getChild(1).getText());

        if (isCalculation(calculation.getChild(0))) {
            operation.addChild(getVariableAssignmentOperation(calculation.getChild(0)));
        } else if (isNumeric(String.valueOf(calculation.getChild(0).getText().indexOf(0)))) {
            operation.addChild(getLiteralWithScalar(calculation.getChild(0).getText()));
        } else {
            operation.addChild(getOperation(String.valueOf(calculation.getChild(0).getText())));
        }

        if (isCalculation(calculation.getChild(2))) {
            operation.addChild(getVariableAssignmentOperation(calculation.getChild(2)));
        } else if (isNumeric(String.valueOf(calculation.getChild(2).getText().indexOf(0)))) {
            operation.addChild(getLiteralWithScalar(calculation.getChild(2).getText()));
        } else {
            operation.addChild(getOperation(String.valueOf(calculation.getChild(2).getText())));
        }
        return operation;
    }

    private Operation getVariableAssignmentOperation(ParseTree calculation, Operation operation) {
        if (isCalculation(calculation)) {
            // Calculation

        } else if (isNumeric(calculation.getText())) {
            // Number
        }

        // Operation
        return null;
    }

    private void addDeclaration(ParseTree declaration) {
        String property = declaration.getChild(0).getText();
        ParseTree value = declaration.getChild(2).getChild(0).getChild(0);

        currentContainer.push(new Declaration(property));

        if (isCalculation(declaration.getChild(2))) {
            currentContainer.push(currentContainer.pop().addChild(getCalculation(declaration.getChild(2))));
        } else if (value.getChildCount() == 1) {
            if (isCalculation(value.getChild(0))) {
                currentContainer.push(currentContainer.pop().addChild(getCalculation(value.getChild(0))));
            } else if (isVariableReference(value)) {
                addVariableReference(value);
            } else {
                addLiteral(value);
            }
        } else if (isVariableReference(value)) {
            addVariableReference(value);
        } else {
            addLiteral(value);
        }
    }

    private Operation getCalculation(ParseTree calculation) {
        Operation operation = getOperation(calculation.getChild(1).getText());

        for (int i = 0; i <= 2; i += 2) {
            ParseTree value = calculation.getChild(i);

            if (value.getChildCount() == 1) {
                value = value.getChild(0);
            }

            if (isVariableReference(value)) {
                operation.addChild(variables.get(value.getText()));
            } else if (isCalculation(value)) {
                operation.addChild(getCalculation(value));
            } else if (isNumeric(value.getText())) {
                operation.addChild(getLiteralWithScalar(value.getText()));
            } else if (getLiteral(value.getText()) != null) {
                operation.addChild(getLiteral(value.getText()));
            } else {
                operation.addChild(getOperation(String.valueOf(value.getText())));
            }
        }
        return operation;
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

    public AST getAST() {
        return ast;
    }

}