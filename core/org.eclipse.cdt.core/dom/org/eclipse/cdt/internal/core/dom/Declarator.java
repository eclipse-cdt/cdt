package org.eclipse.cdt.internal.core.dom;

import java.util.ArrayList;
import java.util.List;

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

	private Expression initialExpression = null; 

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IExpressionOwner#getExpression()
	 */
	public Expression getExpression() {
		return initialExpression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IExpressionOwner#setExpression(org.eclipse.cdt.internal.core.dom.Expression)
	 */
	public void setExpression(Expression exp) {
		initialExpression = exp;
	}
	
	List pointerOperators = null;
	List arrayQualifiers = null; 
	
	
	
	/**
	 * @return List
	 */
	public List getPointerOperators() {
		return pointerOperators;
	}

	public void addPointerOperator( PointerOperator po )
	{
		if( pointerOperators == null )
		{
			pointerOperators = new ArrayList(); 
		}
		pointerOperators.add(po);
	}
	
	List exceptionSpecifier = null; 
	
	public List getExceptionSpecifier()
	{
		return exceptionSpecifier; 
	}

	public void throwsExceptions()
	{
		if( exceptionSpecifier == null )
			exceptionSpecifier = new ArrayList(); 
	}
	
	public void addExceptionSpecifierTypeName( Name name )
	{
		exceptionSpecifier.add( name ); 
	}
	
	boolean isConst = false; 
	boolean isVolatile = false; 
	/**
	 * @return boolean
	 */
	public boolean isConst() {
		return isConst;
	}

	/**
	 * @return boolean
	 */
	public boolean isVolatile() {
		return isVolatile;
	}

	/**
	 * Sets the isConst.
	 * @param isConst The isConst to set
	 */
	public void setConst(boolean isConst) {
		this.isConst = isConst;
	}

	/**
	 * Sets the isVolatile.
	 * @param isVolatile The isVolatile to set
	 */
	public void setVolatile(boolean isVolatile) {
		this.isVolatile = isVolatile;
	}

	/**
	 * @return List
	 */
	public List getArrayQualifiers() {
		return arrayQualifiers;
	}

	public void addArrayQualifier( ArrayQualifier q )
	{
		if( arrayQualifiers == null )
		{
			arrayQualifiers = new ArrayList(); 
		}
		arrayQualifiers.add(q);
	}
	
}
