package org.eclipse.cdt.internal.core.dom;

import org.eclipse.cdt.internal.core.parser.util.DeclSpecifier;
import org.eclipse.cdt.internal.core.parser.util.Name;


public class Declarator implements IExpressionOwner {
	
	public Declarator(DeclSpecifier.Container declaration) {
		this.declaration = declaration;
	}
	
	private DeclSpecifier.Container declaration;
	
	/**
	 * Returns the declaration.
	 * @return SimpleDeclaration
	 */
	public DeclSpecifier.Container getDeclaration() {
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

	private Expression expression = null; 

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IExpressionOwner#getExpression()
	 */
	public Expression getExpression() {
		return expression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IExpressionOwner#setExpression(org.eclipse.cdt.internal.core.dom.Expression)
	 */
	public void setExpression(Expression exp) {
		expression = exp;
	}

}
