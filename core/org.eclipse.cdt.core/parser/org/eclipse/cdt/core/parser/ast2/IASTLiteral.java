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
 * Represents a value of a given type that can be used in an expression.
 * 
 * @author Doug Schaefer
 */
public interface IASTLiteral extends IASTExpression {

	/**
	 * @return the source representation of the value (i.e. as found in
	 * the source code)
	 */
	public String getValue();
	
}
