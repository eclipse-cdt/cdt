package org.eclipse.cdt.internal.core.dom;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.internal.core.parser.util.*;

public class SimpleDeclaration extends Declaration implements DeclarationSpecifier.Container{

	private DeclarationSpecifier declSpec = null;
	 
	public DeclarationSpecifier getDeclSpecifier()
	{
		if( declSpec == null )
			declSpec = new DeclarationSpecifier(); 
		return declSpec;
	}
		
	public void setDeclSpecifier( DeclarationSpecifier in )
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
		getDeclSpecifier().setType(DeclarationSpecifier.t_type);
		this.typeSpecifier = typeSpecifier;
	}

	private List declarators = new LinkedList();
	
	public void addDeclarator(Object declarator) {
		declarators.add(declarator);
	}

	public List getDeclarators() {
		return declarators;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.util.DeclarationSpecifier.Container#removeDeclarator(java.lang.Object)
	 */
	public void removeDeclarator(Object declarator) {
		declarators.remove( declarator );
	}
}
