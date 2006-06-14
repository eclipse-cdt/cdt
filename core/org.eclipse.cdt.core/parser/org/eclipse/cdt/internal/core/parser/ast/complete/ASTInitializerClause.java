/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.internal.core.parser.Parser;
import org.eclipse.cdt.internal.core.parser.ast.EmptyIterator;

/**
 * @author jcamelon
 *
 */
public class ASTInitializerClause implements IASTInitializerClause
{
	private List references = new ArrayList();
    private IASTVariable ownerDeclaration = null;
    private final IASTInitializerClause.Kind kind; 
	private final IASTExpression assignmentExpression; 
	private final List initializerClauses; 
	private final List designators;
	
    /**
     * @param kind
     * @param assignmentExpression
     * @param initializerClauses
     * @param designators
     */
    public ASTInitializerClause(Kind kind, IASTExpression assignmentExpression, List initializerClauses, List designators)
    {
		this.kind = kind; 
		this.assignmentExpression = assignmentExpression;
		this.initializerClauses = initializerClauses; 
		this.designators = designators;
    }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTInitializerClause#getKind()
	 */
	public Kind getKind() {
		return kind;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTInitializerClause#getInitializerList()
	 */
	public Iterator getInitializers() {
		if( initializerClauses == null )
			return EmptyIterator.EMPTY_ITERATOR;
		return initializerClauses.iterator();
	}

	public List getInitializersList(){
	    return ( initializerClauses != null ) ? initializerClauses : Collections.EMPTY_LIST;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTInitializerClause#getAssigmentExpression()
	 */
	public IASTExpression getAssigmentExpression() {
		return assignmentExpression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
	 */
	public void acceptElement(ISourceElementRequestor requestor)
	{
		List initializers = getInitializersList();
		int size = initializers.size();
		for( int i = 0; i < size; i++ )
			((IASTInitializerClause)initializers.get(i)).acceptElement(requestor);
    	
		if( assignmentExpression != null )
			assignmentExpression.acceptElement( requestor );
			
		Parser.processReferences(references, requestor);
		references = null;
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
	 * @see org.eclipse.cdt.core.parser.ast.IASTInitializerClause#getDesignators()
	 */
	public Iterator getDesignators()
	{
		return designators.iterator();
	}
	
	public List getDesignatorList(){
	    return designators;
	}
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTInitializerClause#setOwnerDeclaration(org.eclipse.cdt.core.parser.ast.IASTDeclaration)
     */
    public void setOwnerVariableDeclaration(IASTVariable declaration)
    {
        ownerDeclaration = declaration;
        Iterator subInitializers = getInitializers();
        while( subInitializers.hasNext() )
        	((IASTInitializerClause)subInitializers.next()).setOwnerVariableDeclaration(declaration);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTInitializerClause#getOwnerDeclaration()
     */
    public IASTVariable getOwnerVariableDeclaration()
    {
        return ownerDeclaration;
    }
    
    public List getReferences()
    {
    	return references;
    }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTInitializerClause#findExpressionForDuple(org.eclipse.cdt.core.parser.ITokenDuple)
	 */
	public IASTExpression findExpressionForDuple(ITokenDuple finalDuple) throws ASTNotImplementedException {
		if( kind == IASTInitializerClause.Kind.EMPTY ) return null;
		if( kind == IASTInitializerClause.Kind.ASSIGNMENT_EXPRESSION || 
			kind == Kind.DESIGNATED_ASSIGNMENT_EXPRESSION )
			return ((ASTExpression)assignmentExpression).findNewDescriptor( finalDuple );
		Iterator i = getInitializers();
		while( i.hasNext() )
		{
			IASTInitializerClause clause = (IASTInitializerClause) i.next();
			IASTExpression e = clause.findExpressionForDuple(finalDuple);
			if( e != null ) return e;
		}
		return null;
	}
}
