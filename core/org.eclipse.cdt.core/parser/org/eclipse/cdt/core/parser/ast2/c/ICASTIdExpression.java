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
package org.eclipse.cdt.core.parser.ast2.c;


/**
 * @author Doug Schaefer
 */
public interface ICASTIdExpression extends ICASTExpression {

	/**
	 * @return the name for the id used in the expression
	 */
	public ICASTName getName();
	
	/**
	 * @return the declarator which introduces this name into scope
	 */
	public ICASTDeclarator getDeclarator();
}
