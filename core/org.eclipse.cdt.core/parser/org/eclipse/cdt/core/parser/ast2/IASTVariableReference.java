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
 * Represents a variable reference in an expression. This reference is
 * also an expression whose value is the current value of the variable.
 * 
 * @author Doug Schaefer
 */
public interface IASTVariableReference extends IASTReference, IASTExpression {

	/**
	 * @return the variable this reference is refering to.
	 */
	public IASTVariable getVariable();
	
}
