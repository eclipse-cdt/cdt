/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation */
 *******************************************************************************/

package org.eclipse.cdt.internal.core.parser.ast.quick;

import org.eclipse.cdt.core.parser.ast.ASTUtil;
import org.eclipse.cdt.core.parser.ast.IASTExpression;

/**
 * @author jcamelon
 *
 */
public class ASTEmptyExpression extends ASTExpression implements IASTExpression {

	/**
	 * @param kind
	 */
	public ASTEmptyExpression(Kind kind) {
		super(kind);
		// TODO Auto-generated constructor stub
	}
	
	public String toString(){
		return ASTUtil.getExpressionString( this );
	}
}
