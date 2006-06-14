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

import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.ASTUtil;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;

/**
 * @author jcamelon
 *
 */
public class ASTUnaryTypeIdExpression extends ASTUnaryExpression {
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#freeReferences(org.eclipse.cdt.core.parser.ast.IReferenceManager)
	 */
	public void freeReferences() {
		super.freeReferences();
		typeId.freeReferences();
	}
	private final IASTTypeId typeId;

	/**
	 * @param kind
	 * @param references
	 * @param lhs
	 */
	public ASTUnaryTypeIdExpression(Kind kind, List references, IASTExpression lhs, IASTTypeId typeId) {
		super(kind, references, lhs);
		this.typeId = typeId;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getTypeId()
	 */
	public IASTTypeId getTypeId() {
		return typeId;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ASTExpression#processCallbacks()
	 */
	protected void processCallbacks( ISourceElementRequestor requestor ) {
		super.processCallbacks(requestor );
		typeId.acceptElement( requestor );
	}

	public String toString(){
		return ASTUtil.getExpressionString( this );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ASTUnaryExpression#findOwnerExpressionForIDExpression(org.eclipse.cdt.core.parser.ITokenDuple)
	 */
	public ASTExpression findOwnerExpressionForIDExpression(ITokenDuple duple) {
		if( typeId instanceof ASTTypeId && ((ASTTypeId)typeId).getTokenDuple() == duple )
			return this;

		return super.findOwnerExpressionForIDExpression(duple);
	}
}
