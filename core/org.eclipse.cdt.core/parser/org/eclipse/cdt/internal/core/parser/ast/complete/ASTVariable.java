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
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.internal.core.parser.ast.ASTQualifiedNamedElement;
import org.eclipse.cdt.internal.core.parser.ast.NamedOffsets;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.TypeInfo;

/**
 * @author jcamelon
 *
 */
public class ASTVariable extends ASTSymbol implements IASTVariable
{
	private final boolean previouslyDeclared;
	private final IASTExpression constructorExpression;
    protected final ASTReferenceStore referenceDelegate;
	private final ASTQualifiedNamedElement qualifiedName;
	private NamedOffsets offsets = new NamedOffsets();
    private final IASTExpression bitfieldExpression;
    private final IASTInitializerClause initializerClause;
    private final IASTAbstractDeclaration abstractDeclaration;
    /**
     * @param newSymbol
     * @param abstractDeclaration
     * @param initializerClause
     * @param bitfieldExpression
     * @param startingOffset
     * @param nameOffset
     * @param references
     */
    public ASTVariable(ISymbol newSymbol, IASTAbstractDeclaration abstractDeclaration, IASTInitializerClause initializerClause, IASTExpression bitfieldExpression, int startingOffset, int nameOffset, int nameEndOffset, List references, IASTExpression constructorExpression, boolean previouslyDeclared  )
    {
    	super( newSymbol );
        this.abstractDeclaration = abstractDeclaration;
		this.initializerClause = initializerClause;
		this.bitfieldExpression = bitfieldExpression;
		this.constructorExpression = constructorExpression;
		setStartingOffset( startingOffset );
		setNameOffset( nameOffset );
		setNameEndOffset(nameEndOffset);
		referenceDelegate = new ASTReferenceStore( references );
		qualifiedName = new ASTQualifiedNamedElement( getOwnerScope(), newSymbol.getName() );
		this.previouslyDeclared =previouslyDeclared;		
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#isAuto()
     */
    public boolean isAuto()
    {
        return symbol.getTypeInfo().checkBit( TypeInfo.isAuto );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#isRegister()
     */
    public boolean isRegister()
    {
		return symbol.getTypeInfo().checkBit( TypeInfo.isRegister);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#isStatic()
     */
    public boolean isStatic()
    {
		return symbol.getTypeInfo().checkBit( TypeInfo.isStatic);    	
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#isExtern()
     */
    public boolean isExtern()
    {
		return symbol.getTypeInfo().checkBit( TypeInfo.isExtern );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#isMutable()
     */
    public boolean isMutable()
    {
		return symbol.getTypeInfo().checkBit( TypeInfo.isMutable);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#getAbstractDeclaration()
     */
    public IASTAbstractDeclaration getAbstractDeclaration()
    {
        return abstractDeclaration;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getName()
     */
    public String getName()
    {
        return getSymbol().getName();
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
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameOffset()
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
     * @see org.eclipse.cdt.core.parser.ast.IASTScopedElement#getOwnerScope()
     */
    public IASTScope getOwnerScope()
    {
        return (IASTScope)getSymbol().getContainingSymbol().getASTExtension().getPrimaryDeclaration();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
        try
        {
            requestor.acceptVariable(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
        referenceDelegate.processReferences(requestor);
        if( initializerClause != null )
        	initializerClause.acceptElement(requestor);
        if( constructorExpression != null )
        	constructorExpression.acceptElement(requestor);
		if( getAbstractDeclaration() != null )
			getAbstractDeclaration().acceptElement(requestor);

    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setStartingOffset(int)
     */
    public void setStartingOffset(int o)
    {
        offsets.setStartingOffset(o);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setEndingOffset(int)
     */
    public void setEndingOffset(int o)
    {
        offsets.setEndingOffset(o);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingOffset()
     */
    public int getStartingOffset()
    {
        return offsets.getStartingOffset();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingOffset()
     */
    public int getEndingOffset()
    {
        return offsets.getEndingOffset();
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
	public void setNameEndOffset(int o)
	{
		offsets.setNameEndOffset(o);
	}
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTVariable#getConstructorExpression()
     */
    public IASTExpression getConstructorExpression()
    {
        return constructorExpression;
    }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTNode#lookup(java.lang.String, org.eclipse.cdt.core.parser.ast.IASTNode.LookupKind)
	 */
	public LookupResult lookup(String prefix, LookupKind kind) {
		// TODO Auto-generated method stub
		return null;
	}
}
