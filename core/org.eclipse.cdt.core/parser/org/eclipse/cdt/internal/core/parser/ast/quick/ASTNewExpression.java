/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */

package org.eclipse.cdt.internal.core.parser.ast.quick;

import org.eclipse.cdt.core.parser.ast.ASTUtil;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;

/**
 * @author jcamelon
 *
 */
public class ASTNewExpression extends ASTExpression implements IASTExpression {

	private final IASTNewExpressionDescriptor newDescriptor;
	private final IASTTypeId typeId;

	/**
	 * @param kind
	 * @param newDescriptor
	 * @param typeId
	 */
	public ASTNewExpression(Kind kind, IASTNewExpressionDescriptor newDescriptor, IASTTypeId typeId) {
		super(kind);
		this.newDescriptor = newDescriptor;
		this.typeId = typeId;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getNewExpressionDescriptor()
	 */
	public IASTNewExpressionDescriptor getNewExpressionDescriptor() {
		return newDescriptor;
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
