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
 * Represents a type that has a number of values as represented by
 * member variables. The type may also contain a number of functions
 * that can be called in the context of values of this type.
 *
 * @author Doug Schaefer
 */
public interface IASTCompoundType extends IASTType {

	/**
	 * @return the members of this compound type.
	 */
	public IASTDeclaration[] getMembers();
	
}
