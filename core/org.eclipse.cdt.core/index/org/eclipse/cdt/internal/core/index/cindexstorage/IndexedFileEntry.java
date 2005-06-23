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
package org.eclipse.cdt.internal.core.index.cindexstorage;

import org.eclipse.cdt.internal.core.index.IQueryResult;

/**
 * @author Bogdan Gheorghe
 */
public class IndexedFileEntry implements IQueryResult {
    
    /**
     * Path relative to the workspace root for this file
     */
    private String path;
    /**
     * Unique file id
     */
    private int fileID;
	/**
     * Index Path Var id - links this file entry to a corresponding entry
     * in IndexPathVariableEntry.
     */
    private int pathVarID;
    /**
     * MD5 for this file
     */
    private byte[] MD5;
	
	public IndexedFileEntry(String path, int fileNum) {
		if (fileNum < 1)
			throw new IllegalArgumentException();
		this.fileID= fileNum;
		this.path= path;
	}
	
	/**
	 * Returns the size of the indexedFile.
	 */
	public int footprint() {
		//object+ 4 slots + size of the string (header + 4 slots + char[]) + size of byte array
		return 8 + (4 * 4) + (8 + (4 * 4) + 8 + path.length() * 2); //Put in when you put in MD5 + (8 + MD5.length);
	}
	/**
	 * Returns the file number.
	 */
	public int getFileID() {
		return fileID;
	}
	/**
	 * Returns the path.
	 */
	public String getPath() {
		return path;
	}
	/**
	 * Sets the file number.
	 */
	public void setFileNumber(int fileNumber) {
	    this.fileID= fileNumber;
	}
	public String toString() {
		return "IndexedFile(" + fileID + ": " + path + ")"; //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-3$
	}
	
	
	
}
