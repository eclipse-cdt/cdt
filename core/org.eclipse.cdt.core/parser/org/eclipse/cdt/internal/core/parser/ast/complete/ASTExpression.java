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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.ExpressionEvaluationException;
import org.eclipse.cdt.core.parser.ast.IASTExpression;

/**
 * @author jcamelon
 *
 */
public class ASTExpression implements IASTExpression
{
    private final Kind kind;
    private final IASTExpression lhs;
    private final IASTExpression rhs;
    private final IASTExpression thirdExpression;
    private final String literal;
    private final ITokenDuple typeId;
    private final IASTNewExpressionDescriptor newDescriptor;
    private final List references; 
    private List resultType;
    /**
     * 
     */
    public ASTExpression( Kind kind, IASTExpression lhs, IASTExpression rhs, 
		IASTExpression thirdExpression, ITokenDuple typeId, String literal, IASTNewExpressionDescriptor newDescriptor, List references )
    {
    	this.kind = kind; 
    	this.lhs = lhs;
    	this.rhs = rhs;
    	this.thirdExpression = thirdExpression;
    	this.literal = literal;
    	this.typeId = typeId;
    	this.newDescriptor = newDescriptor;
    	this.references = references;
    	resultType = new ArrayList();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getExpressionKind()
     */
    public Kind getExpressionKind()
    {
        return kind;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getLHSExpression()
     */
    public IASTExpression getLHSExpression()
    {
        return lhs;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getRHSExpression()
     */
    public IASTExpression getRHSExpression()
    {
        return rhs;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getThirdExpression()
     */
    public IASTExpression getThirdExpression()
    {
        return thirdExpression;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getLiteralString()
     */
    public String getLiteralString()
    {
        return literal;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getTypeId()
     */
    public String getTypeIdString()
    {
        return typeId == null ? "" : typeId.toString();
    }
    /*
     * returns the type id token
     */
    public ITokenDuple getTypeId()
    {
    	return typeId;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getNewExpressionDescriptor()
     */
    public IASTNewExpressionDescriptor getNewExpressionDescriptor()
    {
        return newDescriptor;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTExpression#evaluateExpression()
     */
    public int evaluateExpression() throws ExpressionEvaluationException
    {
		throw new ExpressionEvaluationException();
    }
    
    public List getReferences()
    {
    	return references;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
    	if( ! references.isEmpty() )
    	{
	    	ASTReferenceStore store = new ASTReferenceStore( references );
	    	store.processReferences(requestor);
    	}
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
 
	/**
	 * @return
	 */
	public List getResultType() {
		return resultType;
	}

	/**
	 * @param i
	 */
	public void setResultType(List i) {
		resultType = i;
	}

}
