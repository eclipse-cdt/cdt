package org.eclipse.cdt.internal.core.dom;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.internal.core.parser.Token;

public class ClassSpecifier extends TypeSpecifier implements IScope, IOffsetable {

	private AccessSpecifier access = new AccessSpecifier( AccessSpecifier.v_private );
	private ClassKey key = new ClassKey();
	private int startingOffset = 0, totalLength = 0;
	private Token classKeyToken = null;

	public int getClassKey() { return key.getClassKey(); }

	public ClassSpecifier(int classKey, TypeSpecifier.IOwner declaration) {
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

	/**
	 * @return
	 */
	public int getStartingOffset() {
		return startingOffset;
	}

	/**
	 * @return
	 */
	public int getTotalLength() {
		return totalLength;
	}

	/**
	 * @param i
	 */
	public void setStartingOffset(int i) {
		startingOffset = i;
	}

	/**
	 * @param i
	 */
	public void setTotalLength(int i) {
		totalLength = i;
	}

	/**
	 * Returns the classKeyToken.
	 * @return Token
	 */
	public Token getClassKeyToken() {
		return classKeyToken;
	}

	/**
	 * Sets the classKeyToken.
	 * @param classKeyToken The classKeyToken to set
	 */
	public void setClassKeyToken(Token classKeyToken) {
		this.classKeyToken = classKeyToken;
	}

}
