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
package org.eclipse.cdt.internal.core.parser.ast.expression;

import org.eclipse.cdt.core.parser.ast.ASTUtil;
import org.eclipse.cdt.core.parser.ast.IASTExpression;

/**
 * @author aniefer
 */
public class ASTUnaryIdExpression extends ASTIdExpression {
	private final IASTExpression lhs;
	/**
	 * @param kind
	 * @param idExpression
	 */
	public ASTUnaryIdExpression(Kind kind, IASTExpression lhs, String idExpression) {
		super(kind, idExpression);
		this.lhs = lhs;
	}
	
	public IASTExpression getLHSExpression(){
		return lhs;
	}
	
	public String toString(){
		return ASTUtil.getExpressionString( this );
	}
}
