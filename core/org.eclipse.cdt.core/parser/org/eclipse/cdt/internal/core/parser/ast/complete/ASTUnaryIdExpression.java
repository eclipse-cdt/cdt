/**********************************************************************
 * Copyright (c) 2004 IBM - Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
/*
 * Created on Jun 7, 2004
 */
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.ASTUtil;
import org.eclipse.cdt.core.parser.ast.IASTExpression;

/**
 * @author aniefer
 */
public class ASTUnaryIdExpression extends ASTIdExpression {
	private final IASTExpression lhs;
	
	/**
	 * @param kind
	 * @param references
	 * @param idExpression
	 */
	public ASTUnaryIdExpression(Kind kind, List references,	IASTExpression lhs, ITokenDuple idExpression) {
		super(kind, references, idExpression);
		this.lhs = lhs;
	}
	
	public IASTExpression getLHSExpression(){
		return lhs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ASTExpression#findOwnerExpressionForIDExpression(org.eclipse.cdt.core.parser.ITokenDuple)
	 */
	public ASTExpression findOwnerExpressionForIDExpression(ITokenDuple duple) {
		if( isIDExpressionForDuple( lhs, duple )  )
			return this;
		ASTExpression result = recursiveFindExpressionForDuple(lhs, duple);
		return result;
	}
		
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#reconcileReferences()
	 */
	public void reconcileReferences() throws ASTNotImplementedException {
		lhs.reconcileReferences();
		reconcileSubExpression((ASTExpression) lhs);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#purgeReferences()
	 */
	public void purgeReferences() throws ASTNotImplementedException {
		lhs.purgeReferences();
		purgeSubExpression( (ASTExpression) lhs );
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ASTExpression#processCallbacks()
	 */
	protected void processCallbacks( ISourceElementRequestor requestor ) {
		super.processCallbacks(requestor);
		lhs.acceptElement( requestor );
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#freeReferences(org.eclipse.cdt.core.parser.ast.IReferenceManager)
	 */
	public void freeReferences() {
		super.freeReferences();
		lhs.freeReferences();
	}
	
	public String toString(){
		return ASTUtil.getExpressionString( this );
	}
}
