package org.eclipse.cdt.internal.core.dom;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.internal.core.parser.util.AccessSpecifier;
import org.eclipse.cdt.internal.core.parser.util.ClassKey;
import org.eclipse.cdt.internal.core.parser.util.Name;

public class ClassSpecifier extends TypeSpecifier implements IScope {

	AccessSpecifier access = new AccessSpecifier();
	ClassKey key = new ClassKey();  

	public int getClassKey() { return key.getClassKey(); }

	public ClassSpecifier(int classKey, SimpleDeclaration declaration) {
		super(declaration);
		this.key.setClassKey(classKey);
	}
	
	private Name name;
	public void setName(Name n) { name = n; }
	public Name getName() { return name; }
	
	private List baseSpecifiers = new LinkedList();
	public void addBaseSpecifier(BaseSpecifier baseSpecifier) {
		baseSpecifiers.add(baseSpecifier);
	}
	public List getBaseSpecifiers() { return Collections.unmodifiableList(baseSpecifiers); }
	
	private List declarations = new LinkedList();
	
	public void addDeclaration(Declaration declaration) {
		declarations.add(declaration);
	}

	public List getDeclarations() {
		return Collections.unmodifiableList( declarations );
	}
	/**
	 * @return int
	 */
	public int getCurrentVisibility() {
		return access.getAccess();
	}

	/**
	 * Sets the currentVisiblity.
	 * @param currentVisiblity The currentVisiblity to set
	 */
	public void setCurrentVisibility(int currentVisiblity) {
		access.setAccess(currentVisiblity);
	}

}
