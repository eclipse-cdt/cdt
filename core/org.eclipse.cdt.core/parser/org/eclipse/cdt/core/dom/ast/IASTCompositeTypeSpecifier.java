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
 * @author Doug Schaefer
 */
public interface IASTCompositeTypeSpecifier extends IASTDeclSpecifier {

	/**
	 * Is this a struct or a union or some other type of composite type.
	 * 
	 * @return key for this type
	 */
	public int getKey();
	public static final int k_struct = 1;
	public static final int k_union = 2;
	
	/**
	 * Return the name for this composite type.
	 * 
	 * @return
	 */
	public IASTIdentifier getName();
	

}
