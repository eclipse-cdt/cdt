/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */

package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.List;

import org.eclipse.cdt.core.parser.ast.ASTUtil;

/**
 * @author jcamelon
 *
 */
public class ASTLiteralExpression extends ASTExpression {
	
	private final char[] literal;

	/**
	 * @param kind
	 * @param references
	 */
	public ASTLiteralExpression(Kind kind, List references, char[] literal) {
		super(kind, references);
		this.literal = literal;
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
