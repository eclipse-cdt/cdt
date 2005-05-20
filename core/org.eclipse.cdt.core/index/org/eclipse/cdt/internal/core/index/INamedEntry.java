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

public interface INamedEntry extends IIndexEntry, ICIndexStorageEntry  {
	/**
	 * @return Returns the fully qualified name of this entry
	 */
	public char[][] getFullName();
	/**
	 * @return Returns the modifier bit field
	 */
	public int getModifiers();
}
