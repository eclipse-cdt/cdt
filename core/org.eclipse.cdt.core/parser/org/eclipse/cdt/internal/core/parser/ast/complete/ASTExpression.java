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
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.ExpressionEvaluationException;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTReference;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;

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
    private final String literal, idExpression;
    private ITokenDuple idExpressionDuple; 
    private final IASTTypeId typeId;
    private final IASTNewExpressionDescriptor newDescriptor;
    private final List references; 
    private ExpressionResult resultType;
    /**
     * 
     */
    public ASTExpression( Kind kind, IASTExpression lhs, IASTExpression rhs, 
		IASTExpression thirdExpression, IASTTypeId typeId, ITokenDuple idExpression, String literal, IASTNewExpressionDescriptor newDescriptor, List references )
    {
    	this.kind = kind; 
    	this.lhs = lhs;
    	this.rhs = rhs;
    	this.thirdExpression = thirdExpression;
    	this.literal = literal;
    	this.typeId = typeId;
    	this.newDescriptor = newDescriptor;
    	this.references = references;
    	this.idExpressionDuple = idExpression;
    	this.idExpression = idExpressionDuple == null ? "" : idExpressionDuple.toString();
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
    public IASTTypeId getTypeId()
    {
        return typeId;
    }
    /*
     * returns the type id token
     */
    public ITokenDuple getTypeIdTokenDuple()
    {
    	if( typeId == null ) return null;
    	return ((ASTTypeId)typeId).getTokenDuple();
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
		try
        {
            reconcileReferences();
        }
        catch (ASTNotImplementedException e)
        {
        	// will not get thrown
        }
    	if( ! references.isEmpty() )
	    	new ASTReferenceStore( references ).processReferences(requestor);

    	if( typeId != null )
    		typeId.acceptElement(requestor);
    	
    	if( lhs != null )
    		lhs.acceptElement(requestor);
    	
		if( rhs!= null )
			rhs.acceptElement(requestor);
			
		if( thirdExpression != null )
			thirdExpression.acceptElement(requestor);	
	
		if( newDescriptor != null )
			newDescriptor.acceptElement(requestor);
			
		try
		{
			purgeReferences();
		}
		catch (ASTNotImplementedException e)
		{
			// will not get thrown
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
	public ExpressionResult getResultType() {
		return resultType;
	}

	/**
	 * @param i
	 */
	public void setResultType(ExpressionResult i) {
		resultType = i;
	}
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getIdExpression()
     */
    public String getIdExpression()
    {
        return idExpression;
    }
    /**
     * @return
     */
    public ITokenDuple getIdExpressionTokenDuple()
    {
        return idExpressionDuple;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTExpression#reconcileReferences()
     */
    public void reconcileReferences() throws ASTNotImplementedException
    {
    	if( lhs != null )
    		lhs.reconcileReferences();
    	if( rhs != null )
    		rhs.reconcileReferences();
    	if( thirdExpression != null )
    		thirdExpression.reconcileReferences();
    		
        reconcileSubExpression((ASTExpression)lhs);
		reconcileSubExpression((ASTExpression)rhs);
		reconcileSubExpression((ASTExpression)thirdExpression);
    }
    protected void reconcileSubExpression(ASTExpression subExpression)
    {
        if( subExpression != null && subExpression.getReferences() != null )
        {
        	Iterator subExp = subExpression.getReferences().iterator();
        	while( subExp.hasNext() )
        	{
        		IASTReference aReference = (IASTReference)subExp.next();
        		if( aReference != null && references.contains( aReference ) )
        			subExp.remove();
        	}   		
        }
    }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#purgeReferences()
	 */
	public void purgeReferences() throws ASTNotImplementedException
	{
		if( lhs != null )
			lhs.purgeReferences();
		if( rhs != null )
			rhs.purgeReferences();
		if( thirdExpression != null )
			thirdExpression.purgeReferences();
    		
		purgeSubExpression((ASTExpression)lhs);
		purgeSubExpression((ASTExpression)rhs);
		purgeSubExpression((ASTExpression)thirdExpression);
	}
	protected void purgeSubExpression(ASTExpression subExpression)
	{
		if( subExpression != null && subExpression.getReferences() != null )
		{
			subExpression.getReferences().clear();
		}
	}

}
