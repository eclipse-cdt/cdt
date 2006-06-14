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

package org.eclipse.cdt.internal.core.parser.ast.quick;

import org.eclipse.cdt.core.parser.ast.ASTUtil;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;

/**
 * @author jcamelon
 *
 */
public class ASTUnaryTypeIdExpression extends ASTUnaryExpression
		implements
			IASTExpression {

	private final IASTTypeId typeId;

	/**
	 * @param kind
	 * @param lhs
	 * @param typeId
	 */
	public ASTUnaryTypeIdExpression(Kind kind, IASTExpression lhs, IASTTypeId typeId) {
		super( kind, lhs );
		this.typeId = typeId;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getTypeId()
	 */
	public IASTTypeId getTypeId() {
		return typeId;
	}
	
	public String toString(){
		return ASTUtil.getExpressionString( this );
	}
}
