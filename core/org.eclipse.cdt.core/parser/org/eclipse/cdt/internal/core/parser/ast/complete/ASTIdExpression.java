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

import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.ASTUtil;

/**
 * @author jcamelon
 *
 */
public class ASTIdExpression extends ASTExpression {
	
	private ITokenDuple idExpression;
	private String idExpressionValue;
	/**
	 * @param kind
	 * @param references
	 */
	public ASTIdExpression(Kind kind, List references, ITokenDuple idExpression) {
		super(kind, references);
		this.idExpression = idExpression;
		idExpressionValue = idExpression.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getIdExpression()
	 */
	public String getIdExpression() {
		return idExpressionValue;
	}
	
	public ITokenDuple getIdExpressionTokenDuple()
	{
		return idExpression;
	}
	
	public String toString(){
		return ASTUtil.getExpressionString( this );
	}
}
