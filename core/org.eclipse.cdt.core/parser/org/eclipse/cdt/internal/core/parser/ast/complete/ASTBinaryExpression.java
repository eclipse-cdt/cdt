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

package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.ASTUtil;
import org.eclipse.cdt.core.parser.ast.IASTExpression;

/**
 * @author jcamelon
 *
 */
public class ASTBinaryExpression extends ASTUnaryExpression {
	private final IASTExpression rhs;

	/**
	 * @param kind
	 * @param references
	 * @param lhs
	 */
	public ASTBinaryExpression(Kind kind, List references, IASTExpression lhs, IASTExpression rhs) {
		super(kind, references, lhs);
		this.rhs = rhs;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getRHSExpression()
	 */
	public IASTExpression getRHSExpression() {
		return rhs;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ASTExpression#findOwnerExpressionForIDExpression(org.eclipse.cdt.core.parser.ITokenDuple)
	 */
	public ASTExpression findOwnerExpressionForIDExpression(ITokenDuple duple) {
		if( isIDExpressionForDuple( rhs, duple )  )
			return this;
		ASTExpression result = recursiveFindExpressionForDuple(rhs, duple);
		if( result != null )
			return result;
		return super.findOwnerExpressionForIDExpression(duple);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#purgeReferences()
	 */
	public void purgeReferences() throws ASTNotImplementedException {
		super.purgeReferences();
		rhs.purgeReferences();
		purgeSubExpression( (ASTExpression) rhs );
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#reconcileReferences()
	 */
	public void reconcileReferences() throws ASTNotImplementedException {
		super.reconcileReferences();
		rhs.reconcileReferences();
		reconcileSubExpression((ASTExpression) rhs );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ASTExpression#processCallbacks()
	 */
	protected void processCallbacks( ISourceElementRequestor requestor ) {
		super.processCallbacks(requestor );
		rhs.acceptElement( requestor );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#freeReferences(org.eclipse.cdt.core.parser.ast.IReferenceManager)
	 */
	public void freeReferences() {
		super.freeReferences();
		rhs.freeReferences();
	}
	
	public String toString(){
		return ASTUtil.getExpressionString( this );
	}
}
