/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.quick;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTemplate;
import org.eclipse.cdt.internal.core.parser.ast.ASTQualifiedNamedElement;
import org.eclipse.cdt.internal.core.parser.ast.EmptyIterator;
/**
 * @author jcamelon
 *
 */
public class ASTMethod extends ASTFunction implements IASTMethod
{
    private final List constructorChainElements;
    private final boolean isConst;
    private final boolean isDestructor;
    private final boolean isConstructor;
    private final boolean isExplicit;
    private final boolean isPureVirtual;
    private final boolean isVirtual;
    private final boolean isVolatile;
    private final ASTAccessVisibility visibility;
    private final IASTQualifiedNameElement qualifiedName;
    /**
     * @param scope
     * @param name
     * @param parameters
     * @param returnType
     * @param exception
     * @param isInline
     * @param isFriend
     * @param isStatic
     * @param startOffset
     * @param nameOffset
     * @param ownerTemplate
     * @param filename
     */
    public ASTMethod(
        IASTScope scope,
        char[] name,
        List parameters,
        IASTAbstractDeclaration returnType,
        IASTExceptionSpecification exception,
        boolean isInline,
        boolean isFriend,
        boolean isStatic,
        int startOffset,
        int startLine,
        int nameOffset,
        int nameEndOffset,
        int nameLine,
        IASTTemplate ownerTemplate,
        boolean isConst,
        boolean isVolatile,
        boolean isConstructor,
        boolean isDestructor,
        boolean isVirtual, boolean isExplicit, boolean isPureVirtual, 
        ASTAccessVisibility visibility, List constructorChainElements, boolean hasFunctionTryBlock, boolean hasVarArgs, char []filename )
    {
        super(
            scope,
            name,
            parameters, 
            returnType,
            exception,
            isInline,
            isFriend,
            isStatic,
            startOffset,
            startLine,
            nameOffset,
            nameEndOffset, ownerTemplate, hasFunctionTryBlock, hasVarArgs, nameLine, filename);
        this.isVirtual = isVirtual;
        this.isPureVirtual = isPureVirtual;
        this.isConstructor = isConstructor;
        this.isDestructor = isDestructor;
        this.isExplicit = isExplicit; 
        this.isConst = isConst;
        this.isVolatile = isVolatile;
        this.visibility = visibility;
        this.constructorChainElements = constructorChainElements;
        qualifiedName = new ASTQualifiedNamedElement( scope, name );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isVirtual()
     */
    public boolean isVirtual()
    {
        return isVirtual;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isExplicit()
     */
    public boolean isExplicit()
    {
        // TODO Auto-generated method stub
        return isExplicit;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isConstructor()
     */
    public boolean isConstructor()
    {
        return isConstructor;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isDestructor()
     */
    public boolean isDestructor()
    {
        return isDestructor;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isConst()
     */
    public boolean isConst()
    {
        return isConst;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isVolatile()
     */
    public boolean isVolatile()
    {
        return isVolatile;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#isPureVirtual()
     */
    public boolean isPureVirtual()
    {
        return isPureVirtual;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMember#getVisiblity()
     */
    public ASTAccessVisibility getVisiblity()
    {
        return visibility;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement#getFullyQualifiedName()
     */
    public String[] getFullyQualifiedName()
    {
        return qualifiedName.getFullyQualifiedName();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMember#getOwnerClassSpecifier()
     */
     
     
	public void acceptElement( ISourceElementRequestor requestor )
	{
		try
        {
			if( isFriend() )
				requestor.acceptFriendDeclaration(this);
			else
				requestor.acceptMethodDeclaration( this );
        }
        catch (Exception e)
        {
            /* do nothing */
        }
	}
	
	public void enterScope( ISourceElementRequestor requestor )
	{
		try
        {
            requestor.enterMethodBody(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
	}
	
	public void exitScope( ISourceElementRequestor requestor )
	{
		try
        {
            requestor.exitMethodBody(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
	}
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMethod#getConstructorChainInitializers()
     */
    public Iterator getConstructorChainInitializers()
    {
    	if( constructorChainElements == null )
    		return EmptyIterator.EMPTY_ITERATOR; 
        return constructorChainElements.iterator();
    }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTMethod#getOwnerClassSpecifier()
	 */
	public IASTClassSpecifier getOwnerClassSpecifier() {
		return (IASTClassSpecifier) getOwnerScope();
	}
}
