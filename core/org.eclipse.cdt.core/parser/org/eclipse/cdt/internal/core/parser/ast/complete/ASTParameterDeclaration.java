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

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.internal.core.parser.ast.ASTAbstractDeclaration;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;

/**
 * @author jcamelon
 *
 */
public class ASTParameterDeclaration extends ASTSymbol implements IASTParameterDeclaration
{
	private final ASTAbstractDeclaration abstractDeclaration;
    private final String parameterName; 
	private final IASTInitializerClause initializerClause;
    /**
     * @param isConst
     * @param typeSpecifier
     * @param pointerOperators
     * @param arrayModifiers
     * @param parameterName
     * @param initializerClause
     */
    public ASTParameterDeclaration(ISymbol symbol, boolean isConst, boolean isVolatile, IASTTypeSpecifier typeSpecifier, List pointerOperators, List arrayModifiers, List parameters, ASTPointerOperator pointerOp, String parameterName, IASTInitializerClause initializerClause)
    {
    	super( symbol );
    	abstractDeclaration = new ASTAbstractDeclaration( isConst, isVolatile, typeSpecifier, pointerOperators, arrayModifiers, parameters, pointerOp );
		this.parameterName = parameterName;
		this.initializerClause = initializerClause;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration#getName()
     */
    public String getName()
    {
        return parameterName;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration#getDefaultValue()
     */
    public IASTInitializerClause getDefaultValue()
    {
        return initializerClause;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration#isConst()
     */
    public boolean isConst()
    {
        return abstractDeclaration.isConst();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration#isVolatile()
     */
    public boolean isVolatile()
    {
        return abstractDeclaration.isVolatile();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration#getPointerOperators()
     */
    public Iterator getPointerOperators()
    {
        return abstractDeclaration.getPointerOperators();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration#getArrayModifiers()
     */
    public Iterator getArrayModifiers()
    {
        return abstractDeclaration.getArrayModifiers();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration#getParameters()
     */
    public Iterator getParameters()
    {
        return abstractDeclaration.getParameters();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration#getPointerToFunctionOperator()
     */
    public ASTPointerOperator getPointerToFunctionOperator()
    {
        return abstractDeclaration.getPointerToFunctionOperator();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTypeSpecifierOwner#getTypeSpecifier()
     */
    public IASTTypeSpecifier getTypeSpecifier()
    {
        return abstractDeclaration.getTypeSpecifier();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {   
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


    
    
    
}
