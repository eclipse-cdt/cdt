/*******************************************************************************
 * Copyright (c) 2001 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.core.dom;

/**
 * @author jcamelon
 *
 */
public class BitField implements IExpressionOwner {


	public BitField( Declarator owner )
	{
		ownerDeclarator= owner;
	}
	private final Declarator ownerDeclarator; 
	private Expression expression = null; 
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IExpressionOwner#getExpression()
	 */
	public Expression getExpression() {
		return expression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IExpressionOwner#setExpression(org.eclipse.cdt.internal.core.dom.Expression)
	 */
	public void setExpression(Expression exp) {
		expression = exp;
	}

	/**
	 * @return
	 */
	public Declarator getOwnerDeclarator() {
		return ownerDeclarator;
	}

}
