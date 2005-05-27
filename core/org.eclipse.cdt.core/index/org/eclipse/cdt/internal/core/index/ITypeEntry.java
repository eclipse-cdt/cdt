/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.internal.core.index;

/**
 * Interface used to encode type entries for the CIndexStorgage format
 * Type entry constants are defined in IIndex. The list of types are:
 * 
 *  TYPE_CLASS
 *  TYPE_STRUCT
 *  TYPE_UNION
 *  TYPE_ENUM
 *  TYPE_VAR
 *  TYPE_TYPEDEF
 *  TYPE_DERIVED
 *  TYPE_FRIEND
 *  TYPE_FWD_CLASS
 *  TYPE_FWD_STRUCT
 *  TYPE_FWD_UNION
 *  
 * @author bgheorgh
 * @since 3.0
 */
public interface ITypeEntry extends INamedEntry  {
	
	/**
	 * Returns the kind of this type entry
	 * @return int representing type kind defined in IIndex.
	 */
	public int getTypeKind();
	/**
	 * Returns the types that are inherited 
	 * @return an array of char arrays - each representing a separate type that this entry inherits from
	 */
	public IIndexEntry[] getBaseTypes();
	
}
