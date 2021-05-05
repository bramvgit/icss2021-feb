package nl.han.ica.icss.parser;


import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
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

    public AST getAST() {
        return ast;
    }
}