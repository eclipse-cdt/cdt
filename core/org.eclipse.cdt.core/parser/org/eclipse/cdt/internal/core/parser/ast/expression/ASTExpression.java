/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jun 26, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.internal.core.parser.ast.expression;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.ASTExpressionEvaluationException;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.ast.IReferenceManager;



/**
 * @author jcamelon
 */
public class ASTExpression implements IASTExpression {

	private final Kind kind;
	private static final String EMPTY_STRING = "";  //$NON-NLS-1$

	/**
	 * @param kind
	 * @param id
	 */
	public ASTExpression(Kind kind ) {
		this.kind = kind; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getExpressionKind()
	 */
	public Kind getExpressionKind() {
		return kind;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getLHSExpression()
	 */
	public IASTExpression getLHSExpression() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getRHSExpression()
	 */
	public IASTExpression getRHSExpression() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getLiteralString()
	 */
	public String getLiteralString() {
		return EMPTY_STRING;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getTypeId()
	 */
	public IASTTypeId getTypeId() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getNewExpressionDescriptor()
	 */
	public IASTNewExpressionDescriptor getNewExpressionDescriptor() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getThirdExpression()
	 */
	public IASTExpression getThirdExpression() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#evaluateExpression()
	 */
	public long evaluateExpression() throws ASTExpressionEvaluationException {
		// primary expressions
		if( getExpressionKind() == IASTExpression.Kind.PRIMARY_INTEGER_LITERAL )
		{
			try
			{
				if( getLiteralString().startsWith( "0x") || getLiteralString().startsWith( "0x") ) //$NON-NLS-1$ //$NON-NLS-2$
				{
                    return Integer.parseInt( getLiteralString().substring(2), 16 );
				}
				if( getLiteralString().startsWith( "0") && getLiteralString().length() > 1 ) //$NON-NLS-1$
					return Integer.parseInt( getLiteralString().substring(1), 8 );
				return Integer.parseInt( getLiteralString() );
			}
			catch( NumberFormatException nfe )
			{
				throw new ASTExpressionEvaluationException();
			}
		}	
		
		if( getExpressionKind() == IASTExpression.Kind.PRIMARY_BRACKETED_EXPRESSION ) 
			return getLHSExpression().evaluateExpression();
		// unary not 
		if( getExpressionKind() == IASTExpression.Kind.UNARY_NOT_CASTEXPRESSION ) 
			return ( ( getLHSExpression().evaluateExpression() == 0 ) ? 1 : 0 ); 
		
		// multiplicative expressions 
		if( getExpressionKind() == IASTExpression.Kind.MULTIPLICATIVE_MULTIPLY )
			return ( getLHSExpression().evaluateExpression() * getRHSExpression().evaluateExpression()) ; 
		if( getExpressionKind() == IASTExpression.Kind.MULTIPLICATIVE_DIVIDE )
			return ( getLHSExpression().evaluateExpression() / getRHSExpression().evaluateExpression()) ; 
		if( getExpressionKind() == IASTExpression.Kind.MULTIPLICATIVE_MODULUS )
			return ( getLHSExpression().evaluateExpression() % getRHSExpression().evaluateExpression()) ;
		// additives 
		if( getExpressionKind() == IASTExpression.Kind.ADDITIVE_PLUS )
			return ( getLHSExpression().evaluateExpression() + getRHSExpression().evaluateExpression()) ; 
		if( getExpressionKind() == IASTExpression.Kind.ADDITIVE_MINUS )
			return ( getLHSExpression().evaluateExpression() - getRHSExpression().evaluateExpression()) ; 
		// shift expression 
		if( getExpressionKind() == IASTExpression.Kind.SHIFT_LEFT )
			return ( getLHSExpression().evaluateExpression() << getRHSExpression().evaluateExpression()) ; 
		if( getExpressionKind() == IASTExpression.Kind.SHIFT_RIGHT )
			return ( getLHSExpression().evaluateExpression() >> getRHSExpression().evaluateExpression()) ;
		// relational 
		if( getExpressionKind() == IASTExpression.Kind.RELATIONAL_LESSTHAN )
			return ( getLHSExpression().evaluateExpression() < getRHSExpression().evaluateExpression() ? 1 : 0 ) ; 
		if( getExpressionKind() == IASTExpression.Kind.RELATIONAL_GREATERTHAN )
			return ( getLHSExpression().evaluateExpression() > getRHSExpression().evaluateExpression() ? 1 : 0 ) ; 
		if( getExpressionKind() == IASTExpression.Kind.RELATIONAL_LESSTHANEQUALTO )
			return ( getLHSExpression().evaluateExpression() <= getRHSExpression().evaluateExpression() ? 1 : 0 ) ; 
		if( getExpressionKind() == IASTExpression.Kind.RELATIONAL_GREATERTHANEQUALTO )
			return ( getLHSExpression().evaluateExpression() >= getRHSExpression().evaluateExpression() ? 1 : 0 ) ;
		// equality 
		if( getExpressionKind() == IASTExpression.Kind.EQUALITY_EQUALS )
			return ( getLHSExpression().evaluateExpression() == getRHSExpression().evaluateExpression() ? 1 : 0 ) ;  
		if( getExpressionKind() == IASTExpression.Kind.EQUALITY_NOTEQUALS )
			return ( getLHSExpression().evaluateExpression() != getRHSExpression().evaluateExpression() ? 1 : 0 ) ; 
		 // and  
		if( getExpressionKind() == IASTExpression.Kind.ANDEXPRESSION )
			return ( getLHSExpression().evaluateExpression() & getRHSExpression().evaluateExpression() ) ;
		 // xor
		if( getExpressionKind() == IASTExpression.Kind.EXCLUSIVEOREXPRESSION )
			return ( getLHSExpression().evaluateExpression() ^ getRHSExpression().evaluateExpression() ) ;
		// or 
		if( getExpressionKind() == IASTExpression.Kind.INCLUSIVEOREXPRESSION )
			return ( getLHSExpression().evaluateExpression() | getRHSExpression().evaluateExpression() ) ;
		// logical and
		if( getExpressionKind() == IASTExpression.Kind.LOGICALANDEXPRESSION )
			return( ( getLHSExpression().evaluateExpression() != 0 ) &&  ( getRHSExpression().evaluateExpression() != 0 ) ) ? 1 : 0 ;	 
		// logical or  
		if( getExpressionKind() == IASTExpression.Kind.LOGICALOREXPRESSION )
			return( ( getLHSExpression().evaluateExpression() != 0 ) || ( getRHSExpression().evaluateExpression() != 0 ) ) ? 1 : 0 ;
		
		if( getExpressionKind() == IASTExpression.Kind.CONDITIONALEXPRESSION )
		{
			return ( getLHSExpression().evaluateExpression() != 0 ) ? getRHSExpression().evaluateExpression() : getThirdExpression().evaluateExpression(); 
		}

		throw new ASTExpressionEvaluationException();
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager)
    {
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

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getIdExpression()
     */
    public String getIdExpression()
    {
    	return null;
    }
    public char[] getIdExpressionCharArray(){
    	return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTExpression#reconcileReferences()
     */
    public void reconcileReferences(IReferenceManager manager) throws ASTNotImplementedException
    {
    	throw new ASTNotImplementedException();
    }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#purgeReferences()
	 */
	public void purgeReferences() throws ASTNotImplementedException
	{
		throw new ASTNotImplementedException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTNode#lookup(java.lang.String, org.eclipse.cdt.core.parser.ast.IASTNode.LookupKind[], org.eclipse.cdt.core.parser.ast.IASTNode)
	 */
	public ILookupResult lookup(String prefix, LookupKind[] k, IASTNode context, IASTExpression functionParameters) throws LookupError, ASTNotImplementedException {
		// Not provided in this mode
		throw new ASTNotImplementedException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#freeReferences()
	 */
	public void freeReferences(IReferenceManager manager) {
	}


}
