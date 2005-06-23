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

public abstract class CIndexStorageEntry implements ICIndexStorageEntry {

	int meta_kind;
	int entry_type;

	int nameOffset;
	int nameOffsetLength;
	int nameOffsetType;
	
	int elementOffset;
	int elementOffsetLength;
	int elementOffsetType;
	
	int fileNumber;
	
	public CIndexStorageEntry(int meta_kind, int entry_type, int fileNumber){
		this.meta_kind = meta_kind;
		this.entry_type = entry_type; 
		this.fileNumber = fileNumber;
	}
	
	public int getMetaKind() {
		return meta_kind;	
	}
	
	public int getEntryType() {
		return entry_type;
	}

	public int getFileNumber(){
		return fileNumber;
	}
	
	public int getNameOffset(){
		return nameOffset;
	}
	
	public int getNameLength(){
		return nameOffsetLength;
	}
	
	public int getNameOffsetType(){
		return nameOffsetType;
	}

	public int getElementOffset(){
		return elementOffset;
	}
	
	public int getElementLength(){
		return elementOffsetLength; 
	}
	
	public int getElementOffsetType(){
		return elementOffsetType;
	}
	
	/**
	 * Sets the name offset
	 * @param offsetStart - the start of the name offset
	 * @param offsetLength - the length of the name offset
	 * @param offsetType - IIndex.LINE or IIndex.OFFSET
	 */
	public void setNameOffset(int offsetStart, int offsetLength, int offsetType){
		this.nameOffset = offsetStart;
		this.nameOffsetLength = offsetLength;
		this.nameOffsetType = offsetType;
	}
	/**
	 * Sets the element offset
	 * @param offsetStart - the start of the name offset
	 * @param offsetLength - the length of the name offset
	 * @param offsetType - IIndex.LINE or IIndex.OFFSET
	 */
	public void setElementOffset(int offsetStart, int offsetLength, int offsetType){
		this.elementOffset = offsetStart;
		this.elementOffsetLength = offsetLength;
		this.elementOffsetType = offsetType;
	}
	

}
