/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This is a name used in an expression.
 * 
 * @author Doug Schaefer
 */
public interface IASTIdExpression extends IASTExpression {

    public static final ASTNodeProperty ID_NAME = new ASTNodeProperty( "IdExpression Name");  //$NON-NLS-1$
	/**
	 * Returns the name used in the expression.
	 * 
	 * @return the name
	 */
	public IASTName getName();
	
	public void setName( IASTName name );
}
