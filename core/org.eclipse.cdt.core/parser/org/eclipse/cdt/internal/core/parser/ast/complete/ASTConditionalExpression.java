/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
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
public class ASTConditionalExpression extends ASTBinaryExpression {
	private final IASTExpression thirdExpression;

	/**
	 * @param kind
	 * @param references
	 * @param lhs
	 * @param rhs
	 */
	public ASTConditionalExpression(Kind kind, List references,
			IASTExpression lhs, IASTExpression rhs, IASTExpression thirdExpression) {
		super(kind, references, lhs, rhs);
		this.thirdExpression = thirdExpression;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getThirdExpression()
	 */
	public IASTExpression getThirdExpression() {
		return thirdExpression;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ASTExpression#findOwnerExpressionForIDExpression(org.eclipse.cdt.core.parser.ITokenDuple)
	 */
	public ASTExpression findOwnerExpressionForIDExpression(ITokenDuple duple) {
		if( isIDExpressionForDuple( thirdExpression, duple )  )
			return this;
		ASTExpression result = recursiveFindExpressionForDuple(thirdExpression, duple);
		if( result != null )
			return result;
		return super.findOwnerExpressionForIDExpression(duple);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#purgeReferences()
	 */
	public void purgeReferences() throws ASTNotImplementedException {
		super.purgeReferences();
		thirdExpression.purgeReferences();
		purgeSubExpression( (ASTExpression) thirdExpression );
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#reconcileReferences()
	 */
	public void reconcileReferences() throws ASTNotImplementedException {
		super.reconcileReferences();
		thirdExpression.reconcileReferences();
		reconcileSubExpression((ASTExpression) thirdExpression);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ASTExpression#processCallbacks()
	 */
	protected void processCallbacks( ISourceElementRequestor requestor ) {
		super.processCallbacks(requestor );
		thirdExpression.acceptElement( requestor );
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#freeReferences(org.eclipse.cdt.core.parser.ast.IReferenceManager)
	 */
	public void freeReferences() {
		super.freeReferences();
		thirdExpression.freeReferences();
	}
	public String toString(){
		return ASTUtil.getExpressionString( this );
	}
}
