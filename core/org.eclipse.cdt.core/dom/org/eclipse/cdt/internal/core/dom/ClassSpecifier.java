package org.eclipse.cdt.internal.core.dom;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.internal.core.parser.util.Name;

public class ClassSpecifier extends TypeSpecifier implements IScope {

	public static final int t_class = 0;
	public static final int t_struct = 1;
	public static final int t_union = 2;

	public static final int v_public = 0; 
	public static final int v_protected = 1; 
	public static final int v_private = 3; 

	private int currentVisibility;

	private final int classKey;
	public int getClassKey() { return classKey; }

	public ClassSpecifier(int classKey, SimpleDeclaration declaration) {
		super(declaration);
		this.classKey = classKey;
	}
	
	private Name name;
	public void setName(Name n) { name = n; }
	public Name getName() { return name; }
	
	private List baseSpecifiers = new LinkedList();
	public void addBaseSpecifier(BaseSpecifier baseSpecifier) {
		baseSpecifiers.add(baseSpecifier);
	}
	public List getBaseSpecifiers() { return baseSpecifiers; }
	
	private List declarations = new LinkedList();
	
	public void addDeclaration(Declaration declaration) {
		declarations.add(declaration);
	}

	public List getDeclarations() {
		return declarations;
	}
	/**
	 * @return int
	 */
	public int getCurrentVisibility() {
		return currentVisibility;
	}

	/**
	 * Sets the currentVisiblity.
	 * @param currentVisiblity The currentVisiblity to set
	 */
	public void setCurrentVisibility(int currentVisiblity) {
		this.currentVisibility = currentVisiblity;
	}

}
