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
import nl.han.ica.icss.gen.ICSSBaseListener;
import nl.han.ica.icss.gen.ICSSParser;

import java.util.HashMap;
import java.util.Map;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {

    //Accumulator attributes:
    private final AST ast;

    //Use this to keep track of the parent nodes when recursively traversing the ast
    private final IHANStack<ASTNode> currentContainer;

    // Use to keep track of key values for variables
    Map<String, VariableReference> variables;


    public ASTListener() {
        ast = new AST();

        currentContainer = new HANStack<>(10);

        variables = new HashMap<>();
    }

    @Override
    public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
        ast.setRoot((Stylesheet) currentContainer.pop());
    }

    @Override
    public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
        currentContainer.push(new Stylesheet());
    }

    @Override
    public void exitStylerule(ICSSParser.StyleruleContext ctx) {
        Stylerule rule = (Stylerule) currentContainer.pop();
        currentContainer.push(currentContainer.pop().addChild(rule));
    }

    @Override
    public void enterStylerule(ICSSParser.StyleruleContext ctx) {
        currentContainer.push(new Stylerule());
    }

    @Override
    public void exitSelector(ICSSParser.SelectorContext ctx) {
        Selector selector = (Selector) currentContainer.pop();
        currentContainer.push(currentContainer.pop().addChild(selector));
    }

    @Override
    public void enterSelector(ICSSParser.SelectorContext ctx) {
        Selector selector;

        if (ctx.CLASS_IDENT() != null) {
            selector = new ClassSelector(ctx.getText());
        } else if (ctx.ID_IDENT() != null) {
            selector = new IdSelector(ctx.getText());
        } else {
            selector = new TagSelector(ctx.getText());
        }
        currentContainer.push(selector);
    }

    @Override
    public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
        Declaration declaration = (Declaration) currentContainer.pop();
        currentContainer.push(currentContainer.pop().addChild(declaration));
    }

    @Override
    public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
        currentContainer.push(new Declaration(ctx.property().getText()));
    }

    @Override
    public void exitValue(ICSSParser.ValueContext ctx) {
        Literal literal = (Literal) currentContainer.pop();
        currentContainer.push(currentContainer.pop().addChild(literal));
    }

    @Override
    public void enterValue(ICSSParser.ValueContext ctx) {
        Literal literal;

        if (ctx.PIXELSIZE() != null) {
            literal = new PixelLiteral(ctx.getText());
        } else if (ctx.PERCENTAGE() != null) {
            literal = new PercentageLiteral(ctx.getText());
        } else {
            literal = new ColorLiteral(ctx.getText());
        }
        currentContainer.push(literal);
    }

    @Override
    public void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
        VariableAssignment variableAssignment = (VariableAssignment) currentContainer.pop();
        currentContainer.push(currentContainer.pop().addChild(variableAssignment));
    }

    @Override
    public void enterVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
        VariableReference variableReference = new VariableReference(ctx.CAPITAL_IDENT().getText());
        VariableAssignment variableAssignment = new VariableAssignment();
        variableAssignment.name = variableReference;
        variableReference.name = ctx.CAPITAL_IDENT().getText();

        currentContainer.push(variableAssignment);
    }

    @Override
    public void exitVariableValue(ICSSParser.VariableValueContext ctx) {
        if (currentContainer.peek() instanceof Literal) {
            Literal literal = (Literal) currentContainer.pop();
            currentContainer.push(currentContainer.pop().addChild(literal));
        }
    }

    @Override
    public void enterVariableValue(ICSSParser.VariableValueContext ctx) {
        Literal literal = null;

        if (ctx.FALSE() != null || ctx.TRUE() != null) {
            literal = new BoolLiteral(ctx.getText());
        }
        if (literal != null) currentContainer.push(literal);
    }

    @Override
    public void exitVariableReference(ICSSParser.VariableReferenceContext ctx) {
        VariableReference variableReference = (VariableReference) currentContainer.pop();
        currentContainer.push(currentContainer.pop().addChild(variableReference));
    }

    @Override
    public void enterVariableReference(ICSSParser.VariableReferenceContext ctx) {
        currentContainer.push(new VariableReference(ctx.CAPITAL_IDENT().getText()));
    }

    @Override
    public void exitPixelCalculation(ICSSParser.PixelCalculationContext ctx) {
        Operation operation = (Operation) currentContainer.pop();
        currentContainer.push(currentContainer.pop().addChild(operation));
    }

    @Override
    public void enterPixelCalculation(ICSSParser.PixelCalculationContext ctx) {
        Operation operation;

        if (ctx.MUL() != null) {
            operation = new MultiplyOperation();
        } else if (ctx.PLUS() != null) {
            operation = new AddOperation();
        } else {
            operation = new SubtractOperation();
        }
        currentContainer.push(operation);
    }

    @Override
    public void exitPercentageCalculation(ICSSParser.PercentageCalculationContext ctx) {
        Operation operation = (Operation) currentContainer.pop();
        currentContainer.push(currentContainer.pop().addChild(operation));
    }

    @Override
    public void enterPercentageCalculation(ICSSParser.PercentageCalculationContext ctx) {
        Operation operation;

        if (ctx.MUL() != null) {
            operation = new MultiplyOperation();
        } else if (ctx.PLUS() != null) {
            operation = new AddOperation();
        } else {
            operation = new SubtractOperation();
        }
        currentContainer.push(operation);
    }

    @Override
    public void exitScalar(ICSSParser.ScalarContext ctx) {
        ScalarLiteral scalarLiteral = (ScalarLiteral) currentContainer.pop();
        currentContainer.push(currentContainer.pop().addChild(scalarLiteral));
    }

    @Override
    public void enterScalar(ICSSParser.ScalarContext ctx) {
        currentContainer.push(new ScalarLiteral(ctx.SCALAR().getText()));
    }

    @Override
    public void exitPixel(ICSSParser.PixelContext ctx) {
        PixelLiteral pixelLiteral = (PixelLiteral) currentContainer.pop();
        currentContainer.push(currentContainer.pop().addChild(pixelLiteral));
    }

    @Override
    public void enterPixel(ICSSParser.PixelContext ctx) {
        currentContainer.push(new PixelLiteral(ctx.PIXELSIZE().getText()));
    }

    @Override
    public void exitPercent(ICSSParser.PercentContext ctx) {
        PercentageLiteral percentageLiteral = (PercentageLiteral) currentContainer.pop();
        currentContainer.push(currentContainer.pop().addChild(percentageLiteral));
    }

    @Override
    public void enterPercent(ICSSParser.PercentContext ctx) {
        currentContainer.push(new PercentageLiteral(ctx.PERCENTAGE().getText()));
    }

    @Override
    public void exitIf_clause(ICSSParser.If_clauseContext ctx) {
        IfClause ifClause = (IfClause) currentContainer.pop();
        currentContainer.push(currentContainer.pop().addChild(ifClause));
    }

    @Override
    public void enterIf_clause(ICSSParser.If_clauseContext ctx) {
        currentContainer.push(new IfClause());
    }

    @Override
    public void exitElse_clause(ICSSParser.Else_clauseContext ctx) {
        ElseClause elseClause = (ElseClause) currentContainer.pop();
        currentContainer.push(currentContainer.pop().addChild(elseClause));
    }

    @Override
    public void enterElse_clause(ICSSParser.Else_clauseContext ctx) {
        currentContainer.push(new ElseClause());
    }

    public AST getAST() {
        return ast;
    }
}