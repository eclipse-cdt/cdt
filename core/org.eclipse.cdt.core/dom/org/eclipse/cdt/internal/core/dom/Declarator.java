package org.eclipse.cdt.internal.core.dom;

import org.eclipse.cdt.internal.core.parser.util.*;
import org.eclipse.cdt.internal.core.parser.util.Name;


public class Declarator {
	
	public Declarator(DeclarationSpecifier.Container declaration) {
		this.declaration = declaration;
	}
	
	private DeclarationSpecifier.Container declaration;
	
	/**
	 * Returns the declaration.
	 * @return SimpleDeclaration
	 */
	public DeclarationSpecifier.Container getDeclaration() {
		return declaration;
	}

	/**
	 * Sets the declaration.
	 * @param declaration The declaration to set
	 */
	public void setDeclaration(SimpleDeclaration declaration) {
		this.declaration = declaration;
	}

	private Name name;
	
	/**
	 * Returns the name.
	 * @return Name
	 */
	public Name getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(Name name) {
		this.name = name;
	}
	
	ParameterDeclarationClause parms = null; 

	public void addParms( ParameterDeclarationClause parms )
	{
		this.parms = parms; 
	}	
	
	/**
	 * Returns the parms.
	 * @return ParameterDeclarationClause
	 */
	public ParameterDeclarationClause getParms() {
		return parms;
	}

}
