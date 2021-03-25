package nl.han.ica.icss.parser;


import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {

    //Accumulator attributes:
    private AST ast;

    //Use this to keep track of the parent nodes when recursively traversing the ast
    private IHANStack<ASTNode> currentContainer;

    public ASTListener() {
        ast = new AST();
        currentContainer = new HANStack<>(10);
    }

    @Override
    public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
        ctx.children.forEach(child -> {
            if (isBlock(child)) {
                addSelector(child);
                addDeclarations(child);
            }
            ast.root.addChild(currentContainer.pop());
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
                    Literal valueLiteral;

                    // Get values (color, pixel, literal, etc)
                    if (value.equalsIgnoreCase("true")) {
                        valueLiteral = new BoolLiteral(true);
                    } else if (value.equalsIgnoreCase("false")) {
                        valueLiteral = new BoolLiteral(false);
                    } else if (value.endsWith("px")) {
                        valueLiteral = new PixelLiteral(value);
                    } else if (value.endsWith("%")) {
                        valueLiteral = new PercentageLiteral(value);
                    } else if (value.startsWith("#")) {
                        valueLiteral = new ColorLiteral(value);
                    } else {
                        valueLiteral = new ScalarLiteral(value);
                    }

                    // Make new declaration with everything above
                    declarations.add(new Declaration(property).addChild(valueLiteral));
                }
            }
        }
        currentContainer.push(new Stylerule((Selector) currentContainer.pop(), declarations));
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

    private boolean isBlock(ParseTree child) {
        return child.getChildCount() >= 4;
    }

    public AST getAST() {
        return ast;
    }

}