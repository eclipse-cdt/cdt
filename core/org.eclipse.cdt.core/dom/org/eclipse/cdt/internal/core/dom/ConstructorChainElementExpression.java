/**********************************************************************
 * Created on Mar 28, 2003
 *
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.dom;

/**
 * @author jcamelon
 *
 */
public class ConstructorChainElementExpression implements IExpressionOwner {
	
	Expression exp; 
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IExpressionOwner#getExpression()
	 */
	public Expression getExpression() {
		return exp;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IExpressionOwner#setExpression(org.eclipse.cdt.internal.core.dom.Expression)
	 */
	public void setExpression(Expression exp) {
		this.exp = exp;
	}

	ConstructorChainElementExpression( ConstructorChainElement element )
	{
		this.ownerElement = element;
	}
	
	private final ConstructorChainElement ownerElement; 
	
	/**
	 * @return ConstructorChainElement
	 */
	public ConstructorChainElement getOwnerElement() {
		return ownerElement;
	}

}
