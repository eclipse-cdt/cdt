/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.quick;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IReferenceManager;
import org.eclipse.cdt.internal.core.parser.ast.ASTQualifiedNamedElement;
import org.eclipse.cdt.internal.core.parser.ast.NamedOffsets;

/**
 * @author jcamelon
 *
 */
public class ASTVariable extends ASTDeclaration implements IASTVariable
{
    private IASTExpression constructorExpression;
    private final boolean isAuto;
    private final IASTInitializerClause initializerClause;
    private final IASTExpression bitfieldExpression;
    private final IASTAbstractDeclaration abstractDeclaration;
    private final boolean isMutable;
    private final boolean isExtern;
    private final boolean isRegister;
    private final boolean isStatic;
    private final String name;
    private NamedOffsets offsets = new NamedOffsets();
    private final ASTQualifiedNamedElement qualifiedName; 
    /**
     * @param scope
     */
    public ASTVariable(IASTScope scope, String name, boolean isAuto, IASTInitializerClause initializerClause, IASTExpression bitfieldExpression, 
    	IASTAbstractDeclaration abstractDeclaration, boolean isMutable, boolean isExtern, boolean isRegister, boolean isStatic, int startingOffset, int startLine, int nameOffset, int nameEndOffset, int nameLine, IASTExpression constructorExpression )
    {
        super(scope);
		this.isAuto = isAuto;
		this.initializerClause = initializerClause;
		this.bitfieldExpression = bitfieldExpression;
		this.abstractDeclaration = abstractDeclaration;
		this.isMutable= isMutable;
		this.isExtern = isExtern;
		this.isRegister = isRegister;
		this.isStatic = isStatic;
		this.name = name;
		this.constructorExpression = constructorExpression;
		qualifiedName = new ASTQualifiedNamedElement( scope, name );
		setStartingOffsetAndLineNumber(startingOffset, startLine);
		setNameOffset(nameOffset);
		setNameEndOffsetAndLineNumber( nameEndOffset, nameLine );
		if( initializerClause != null )
			initializerClause.setOwnerVariableDeclaration(this);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#isAuto()
     */
    public boolean isAuto()
    {
        return isAuto;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#isRegister()
     */
    public boolean isRegister()
    {
        return isRegister;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#isStatic()
     */
    public boolean isStatic()
    {
        return isStatic;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#isExtern()
     */
    public boolean isExtern()
    {
        return isExtern;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#isMutable()
     */
    public boolean isMutable()
    {
        return isMutable;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#getAbstractDeclaration()
     */
    public IASTAbstractDeclaration getAbstractDeclaration()
    {
        return abstractDeclaration;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#getName()
     */
    public String getName()
    {
        return name;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#getInitializerClause()
     */
    public IASTInitializerClause getInitializerClause()
    {
        return initializerClause;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#isBitfield()
     */
    public boolean isBitfield()
    {
        return ( bitfieldExpression != null );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#getBitfieldExpression()
     */
    public IASTExpression getBitfieldExpression()
    {
        return bitfieldExpression;
    }
	/* (non-Javadoc)
	* @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setStartingOffset(int)
	*/
   public void setStartingOffsetAndLineNumber(int offset, int lineNumber)
   {
	   offsets.setStartingOffsetAndLineNumber(offset, lineNumber);
   }
   /* (non-Javadoc)
	* @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setEndingOffset(int)
	*/
   public void setEndingOffsetAndLineNumber(int offset, int lineNumber)
   {
	   offsets.setEndingOffsetAndLineNumber(offset, lineNumber);
   }
   /* (non-Javadoc)
	* @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getElementStartingOffset()
	*/
   public int getStartingOffset()
   {
	   return offsets.getStartingOffset();
   }
   /* (non-Javadoc)
	* @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getElementEndingOffset()
	*/
   public int getEndingOffset()
   {
	   return offsets.getEndingOffset();
   }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getElementNameOffset()
	 */
	public int getNameOffset()
	{
	    return offsets.getNameOffset();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameOffset(int)
	 */
	public void setNameOffset(int o)
	{
	    offsets.setNameOffset(o);
	}
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement#getFullyQualifiedName()
     */
    public String[] getFullyQualifiedName()
    {
        return qualifiedName.getFullyQualifiedName();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#accept(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    	try
        {
            requestor.acceptVariable(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enter(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exit(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#getConstructorExpression()
     */
    public IASTExpression getConstructorExpression()
    {
        return constructorExpression;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameEndOffset()
	 */
	public int getNameEndOffset()
	{
		return offsets.getNameEndOffset();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameEndOffset(int)
	 */
	public void setNameEndOffsetAndLineNumber(int offset, int lineNumber)
	{
		offsets.setNameEndOffsetAndLineNumber(offset, lineNumber);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingLine()
	 */
	public int getStartingLine() {
		return offsets.getStartingLine();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingLine()
	 */
	public int getEndingLine() {
		return offsets.getEndingLine();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameLineNumber()
	 */
	public int getNameLineNumber() {
		return offsets.getNameLineNumber();
	}
}

