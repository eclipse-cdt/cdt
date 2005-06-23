/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.index;

public interface ICIndexStorageEntry {
	
	/**
	 * @return Returns the metakind of this entry as defined in IIndex
	 */
	public int getMetaKind();
	/**
	 * @return Returns the file number associated with this particular entry or 0 if file number has not been set
	 */
	public int getFileNumber();
	/**
	 * @return Returns the starting offset for the name of this entry or 0 if the offset has not been set
	 */
	public int getNameOffset();
	/**
	 * @return Returns the length of the name of this entry or 0 if the offset has not been set
	 */
	public int getNameLength();
	/**
	 * @returns Returns the offset type for the name offset - IIndex.LINE or IIndex.OFFSET
	 */
	public int getNameOffsetType();
	/**
	 * @return Returns the starting offset for the entry
	 */
	public int getElementOffset();
	/**
	 * @returns Returns the length of this entry
	 */
	public int getElementLength();
	/**
	 * @returns Returns the offset type for this element offset - IIndex.LINE or IIndex.OFFSET
	 */
	public int getElementOffsetType();
}
