package org.eclipse.cdt.internal.core.dom;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class SimpleDeclaration extends Declaration implements DeclSpecifier.IContainer, TypeSpecifier.IOwner {

	private AccessSpecifier accessSpecifier = null;
	private DeclSpecifier declSpec = null;
	private boolean isFunctionDefinition = false;
	 
	 
	public SimpleDeclaration(IScope owner )
	{
		super( owner );
	}
	
	public DeclSpecifier getDeclSpecifier()
	{
		if( declSpec == null )
			declSpec = new DeclSpecifier(); 
		return declSpec;
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
}
