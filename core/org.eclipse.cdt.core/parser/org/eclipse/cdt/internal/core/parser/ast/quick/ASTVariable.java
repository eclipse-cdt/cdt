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
    private final char[] name;
    private final ASTQualifiedNamedElement qualifiedName; 
    private final char [] fn;
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getFilename()
	 */
	public char[] getFilename() {
		return fn;
	}

    /**
     * @param scope
     * @param filename
     */
    public ASTVariable(IASTScope scope, char[] name, boolean isAuto, IASTInitializerClause initializerClause, IASTExpression bitfieldExpression, 
    	IASTAbstractDeclaration abstractDeclaration, boolean isMutable, boolean isExtern, boolean isRegister, boolean isStatic, int startingOffset, int startLine, int nameOffset, int nameEndOffset, int nameLine, IASTExpression constructorExpression, char[] filename )
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
		fn = filename;
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
        return String.valueOf(name);
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
    
	private int startingLineNumber, startingOffset, endingLineNumber, endingOffset, nameStartOffset, nameEndOffset, nameLineNumber;
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingLine()
     */
    public final int getStartingLine() {
    	return startingLineNumber;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingLine()
     */
    public final int getEndingLine() {
    	return endingLineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameLineNumber()
     */
    public final int getNameLineNumber() {
    	return nameLineNumber;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setStartingOffset(int)
     */
    public final void setStartingOffsetAndLineNumber(int offset, int lineNumber)
    {
    	startingOffset = offset;
    	startingLineNumber = lineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setEndingOffset(int)
     */
    public final void setEndingOffsetAndLineNumber(int offset, int lineNumber)
    {
    	endingOffset = offset;
    	endingLineNumber = lineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingOffset()
     */
    public final int getStartingOffset()
    {
        return startingOffset;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingOffset()
     */
    public final int getEndingOffset()
    {
        return endingOffset;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameOffset()
     */
    public final int getNameOffset()
    {
    	return nameStartOffset;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameOffset(int)
     */
    public final void setNameOffset(int o)
    {
        nameStartOffset = o;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameEndOffset()
     */
    public final int getNameEndOffset()
    {
        return nameEndOffset;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameEndOffset(int)
     */
    public final void setNameEndOffsetAndLineNumber(int offset, int lineNumber)
    {
    	nameEndOffset = offset;
    	nameLineNumber = lineNumber;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameCharArray()
     */
    public char[] getNameCharArray() {
        return name;
    }
}

