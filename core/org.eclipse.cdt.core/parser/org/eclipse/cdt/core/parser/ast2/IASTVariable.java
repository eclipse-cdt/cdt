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
package org.eclipse.cdt.core.parser.ast2;

/**
 * Represents a variable definition.
 * 
 * @author Doug Schaefer
 */
public interface IASTVariable {

	/**
	 * @return the type of the variable
	 */
	public IASTType getType();
	
	/**
	 * @return the expression that initializes the variable when it is
	 * instantiated.
	 */
	public IASTExpression getInitialization();

}
