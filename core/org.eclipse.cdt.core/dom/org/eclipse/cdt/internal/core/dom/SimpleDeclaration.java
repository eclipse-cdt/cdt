package org.eclipse.cdt.internal.core.dom;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class SimpleDeclaration extends Declaration implements DeclSpecifier.Container, IOffsetable, TypeSpecifier.IOwner {

	private int startingOffset = 0, totalLength = 0;
	private AccessSpecifier accessSpecifier = null;
	private DeclSpecifier declSpec = null;
	private boolean isFunctionDefinition = false;
	 
	public DeclSpecifier getDeclSpecifier()
	{
		if( declSpec == null )
			declSpec = new DeclSpecifier(); 
		return declSpec;
	}
		
	public void setDeclSpecifier( DeclSpecifier in )
	{
		declSpec = in; 
	} 

	/**
	 * This is valid when the type is t_type.  It points to a
	 * classSpecifier, etc.
	 */
	private TypeSpecifier typeSpecifier;
	
	/**
	 * Returns the typeSpecifier.
	 * @return TypeSpecifier
	 */
	public TypeSpecifier getTypeSpecifier() {
		return typeSpecifier;
	}

	/**
	 * Sets the typeSpecifier.
	 * @param typeSpecifier The typeSpecifier to set
	 */
	public void setTypeSpecifier(TypeSpecifier typeSpecifier) {
		getDeclSpecifier().setType(DeclSpecifier.t_type);
		this.typeSpecifier = typeSpecifier;
	}

	private List declarators = new LinkedList();
	
	public void addDeclarator(Object declarator) {
		declarators.add(declarator);
	}

	public List getDeclarators() {
		return Collections.unmodifiableList( declarators );
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.util.DeclarationSpecifier.Container#removeDeclarator(java.lang.Object)
	 */
	public void removeDeclarator(Object declarator) {
		declarators.remove( declarator );
	}
	/**
	 * @return
	 */
	public AccessSpecifier getAccessSpecifier() {
		return accessSpecifier;
	}

	/**
	 * @param specifier
	 */
	public void setAccessSpecifier(AccessSpecifier specifier) {
		accessSpecifier = specifier;
	}

	/**
	 * @return
	 */
	public boolean isFunctionDefinition() {
		return isFunctionDefinition;
	}

	/**
	 * @param b
	 */
	public void setFunctionDefinition(boolean b) {
		isFunctionDefinition = b;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IOffsettable#getStartingOffset()
	 */
	public int getStartingOffset() {
		return startingOffset;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IOffsettable#getTotalLength()
	 */
	public int getTotalLength() {
		return totalLength;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IOffsettable#setStartingOffset(int)
	 */
	public void setStartingOffset(int i) {
		startingOffset = i;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IOffsettable#setTotalLength(int)
	 */
	public void setTotalLength(int i) {
		totalLength = i;
	}

}
