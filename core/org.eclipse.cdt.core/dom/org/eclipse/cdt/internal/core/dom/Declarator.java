package org.eclipse.cdt.internal.core.dom;

import java.util.ArrayList;
import java.util.Collections;
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
	
	List pointerOperators = new ArrayList();
	List arrayQualifiers = new ArrayList(); 
	
	/**
	 * @return List
	 */
	public List getPointerOperators() {
		return Collections.unmodifiableList(pointerOperators);
	}

	public void addPointerOperator( PointerOperator po )
	{
		pointerOperators.add(po);
	}
	
	ExceptionSpecifier exceptionSpecifier = new ExceptionSpecifier(); 
	
	public ExceptionSpecifier getExceptionSpecifier()
	{
		return exceptionSpecifier; 
	}
	
	boolean isConst = false; 
	boolean isVolatile = false;
	boolean isPureVirtual = false; 
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
		return Collections.unmodifiableList( arrayQualifiers );
	}

	public void addArrayQualifier( ArrayQualifier q )
	{
		arrayQualifiers.add(q);
	}
	
	private ConstructorChain ctorChain = null;
	
	/**
	 * @return ConstructorChain
	 */
	public ConstructorChain getCtorChain() {
		return ctorChain;
	}

	/**
	 * Sets the ctorChain.
	 * @param ctorChain The ctorChain to set
	 */
	public void setCtorChain(ConstructorChain ctorChain) {
		this.ctorChain = ctorChain;
	}

	/**
	 * @return boolean
	 */
	public boolean isPureVirtual() {
		return isPureVirtual;
	}

	/**
	 * Sets the isPureVirtual.
	 * @param isPureVirtual The isPureVirtual to set
	 */
	public void setPureVirtual(boolean isPureVirtual) {
		this.isPureVirtual = isPureVirtual;
	}

}
