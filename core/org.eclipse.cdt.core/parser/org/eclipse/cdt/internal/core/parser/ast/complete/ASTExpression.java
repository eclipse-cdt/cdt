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
import org.eclipse.cdt.core.parser.ast.ASTExpressionEvaluationException;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTReference;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.ast.IReferenceManager;
import org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ITypeInfo;
import org.eclipse.cdt.internal.core.parser.pst.TypeInfoProvider;

/**
 * @author jcamelon
 *
 */
public abstract class ASTExpression extends ASTNode implements IASTExpression
{
    private final Kind kind;
    private List references;
    private ExpressionResult resultType;
    
    /**
     * 
     */
    public ASTExpression( Kind kind, List references )
    {
    	this.kind = kind; 
    	this.references = references;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getExpressionKind()
     */
    public Kind getExpressionKind()
    {
        return kind;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTExpression#evaluateExpression()
     */
    public long evaluateExpression() throws ASTExpressionEvaluationException
    {
		throw new ASTExpressionEvaluationException();
    }
    
    public List getReferences()
    {
    	return references;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager)
    {
		try
        {
            reconcileReferences(manager);
        }
        catch (ASTNotImplementedException e)
        {
        	// will not get thrown
        }
        manager.processReferences( references, requestor );
        references = null;
    
		processCallbacks(requestor, manager);
			
		try
		{
			purgeReferences();
		}
		catch (ASTNotImplementedException e)
		{
			// will not get thrown
		}
    }
    
    /**
     * @param requestor TODO
     * @param manager TODO
	 * 
	 */
	protected void processCallbacks(ISourceElementRequestor requestor, IReferenceManager manager) {
	}

	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor, IReferenceManager manager)
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
     * @see org.eclipse.cdt.core.parser.ast.IASTExpression#reconcileReferences()
     */
    public void reconcileReferences(IReferenceManager manager) throws ASTNotImplementedException
    {
    }
    
    protected void reconcileSubExpression(ASTExpression subExpression, IReferenceManager manager)
    {
        if( subExpression != null && subExpression.getReferences() != null )
        {
        	Iterator subExp = subExpression.getReferences().iterator();
        	while( subExp.hasNext() )
        	{
        		IASTReference aReference = (IASTReference)subExp.next();
        		if( aReference != null && references.contains( aReference ) )
        		{
        			subExp.remove();
        			manager.returnReference( aReference );
        		}
        	}   		
        }
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#purgeReferences()
	 */
	public void purgeReferences() throws ASTNotImplementedException
	{
	}
	
	protected void purgeSubExpression(ASTExpression subExpression)
	{
		if( subExpression != null && subExpression.getReferences() != null )
		{
			subExpression.getReferences().clear();
		}
	}

	
	protected String getStringPrefix()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append( "ASTExpression w/Kind=" ); //$NON-NLS-1$
		buffer.append( kind.getKindName() );
		return buffer.toString();
	}
	
	
	public IContainerSymbol getLookupQualificationSymbol() throws LookupError {
		ExpressionResult result = getResultType();
		ITypeInfo type = (result != null ) ? result.getResult() : null;
		IContainerSymbol containerSymbol = null;
		
		if( type != null && type.getTypeSymbol() != null ){
			TypeInfoProvider provider = type.getTypeSymbol().getSymbolTable().getTypeInfoProvider();
			type = type.getFinalType( provider );
			if( type.isType( ITypeInfo.t_type ) && 
				type.getTypeSymbol() != null   && type.getTypeSymbol() instanceof IContainerSymbol )
			{
				containerSymbol = (IContainerSymbol) type.getTypeSymbol();
			}
			provider.returnTypeInfo( type );
		}
				
		return containerSymbol;
	}
	
	public boolean shouldFilterLookupResult( ISymbol symbol ){
		ExpressionResult result = getResultType();
		ITypeInfo type = ( result != null ) ? result.getResult() : null;
		if( type != null ){
			boolean answer = false;
			TypeInfoProvider provider = symbol.getSymbolTable().getTypeInfoProvider(); 
			type = type.getFinalType( provider );
			if( type.checkBit( ITypeInfo.isConst ) && !symbol.getTypeInfo().checkBit( ITypeInfo.isConst ) )	
				answer = true;
			
			if( type.checkBit( ITypeInfo.isVolatile ) && !symbol.getTypeInfo().checkBit( ITypeInfo.isVolatile ) )
				answer = true;
			
			provider.returnTypeInfo( type );
			return answer;
		}
		
		return false;
	}
	
	/**
	 * @param duple
	 * @return
	 */
	public ASTExpression findOwnerExpressionForIDExpression(ITokenDuple duple) {
		return null;
	}
	
	/**
	 * @param duple
	 * @return
	 */
	protected ASTExpression recursiveFindExpressionForDuple(IASTExpression expression, ITokenDuple duple) {
		if( expression == null ) return null;
		return ((ASTExpression)expression).findOwnerExpressionForIDExpression(duple);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ASTExpression#isIDExpressionForDuple(org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ITokenDuple)
	 */
	protected boolean isIDExpressionForDuple(IASTExpression expression,
			ITokenDuple duple) {
		if( expression == null ) return false;
		if( expression.getExpressionKind() == IASTExpression.Kind.ID_EXPRESSION &&
			expression instanceof ASTIdExpression )
		{
			ITokenDuple expressionDuple = ((ASTIdExpression)expression).getIdExpressionTokenDuple();
			// check equality
			if( expressionDuple.equals( duple ) )
				return true;
			// check subduple
			if( expressionDuple.contains( duple ) )
				return true;
		}
		return false;
	}

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	public IASTExpression getLHSExpression() {
		return null;
	}

	public IASTExpression getRHSExpression() {
		return null;
	}

	public IASTExpression getThirdExpression() {
		return null;
	}

	public String getLiteralString() {
		return EMPTY_STRING;
	}

	public String getIdExpression() {
		return EMPTY_STRING;
	}

	public IASTTypeId getTypeId() {
		return null;
	}

	public IASTNewExpressionDescriptor getNewExpressionDescriptor() {
		return null;
	}

	/**
	 * @param finalDuple
	 * @return
	 */
	public IASTExpression findNewDescriptor(ITokenDuple finalDuple) {
		return null;
	}
	
	public void freeReferences( IReferenceManager manager )
	{
		if( references == null || references.isEmpty() ) return;
		for (int i = 0; i < references.size(); ++i)
			manager.returnReference( (IASTReference) references.get(i));
		references.clear();
	}
}
