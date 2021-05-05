package nl.han.ica.icss.ast;

import nl.han.ica.icss.visitor.Visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Stylerule extends ASTNode {
	
	public ArrayList<Selector> selectors = new ArrayList<>();
	public ArrayList<ASTNode> body = new ArrayList<>();

    public Stylerule() { }

    public Stylerule(Selector selector, ArrayList<ASTNode> body) {

    	this.selectors = new ArrayList<>();
    	this.selectors.add(selector);
    	this.body = body;
    }

    @Override
	public String getNodeLabel() {
		return "Stylerule";
	}
	@Override
	public ArrayList<ASTNode> getChildren() {
		ArrayList<ASTNode> children = new ArrayList<>();
		children.addAll(selectors);
		children.addAll(body);

		return children;
	}

    @Override
    public ASTNode addChild(ASTNode child) {
		if(child instanceof Selector)
			selectors.add((Selector) child);
		else
        	body.add(child);

		return this;
    }
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Stylerule stylerule = (Stylerule) o;
        return Objects.equals(selectors, stylerule.selectors) &&
                Objects.equals(body, stylerule.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selectors, body);
    }

    public void accept(List<Visitor> visitors) {
        visitors.forEach(visitor -> visitor.visit(this));
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
