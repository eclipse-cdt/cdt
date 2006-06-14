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


/**
 * @author jcamelon
 *
 */
public class ASTLiteralExpression extends ASTExpression {

	private final char[] literal;

	/**
	 * @param kind
	 * @param literal
	 */
	public ASTLiteralExpression(Kind kind, char[] literal) {
		super( kind );
		this.literal =literal;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getLiteralString()
	 */
	public String getLiteralString() {
		return String.valueOf( literal );
	}
	
	public String toString(){
		return ASTUtil.getExpressionString( this );
	}
}
