/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */

package org.eclipse.cdt.internal.core.parser.ast.expression;

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
