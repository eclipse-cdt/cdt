/**********************************************************************
 * Created on Mar 23, 2003
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
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ArrayQualifier implements IExpressionOwner {

	private Expression constantExpression; 

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IExpressionOwner#getExpression()
	 */
	public Expression getExpression() {
		return constantExpression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IExpressionOwner#setExpression(org.eclipse.cdt.internal.core.dom.Expression)
	 */
	public void setExpression(Expression exp) {
		constantExpression = exp;
	}	
	
	public ArrayQualifier( Declarator owner )
	{
		ownerDeclarator = owner;
	}
	
	private Declarator ownerDeclarator; 
	/**
	 * @return Declarator
	 */
	public Declarator getOwnerDeclarator() {
		return ownerDeclarator;
	}

}
