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
package org.eclipse.cdt.internal.core.index.cindexstorage;

/**
 * @author Bogdan Gheorghe
 */
public class IndexPathVariableEntry {
    //All indexes will reserve 1 in the PathVariable block to represent
    //the workspace location
    public static final int WORKSPACE_ID = 1;
    
    private String	pathVariableName;
    private String	pathVariablePath;
    private int 	pathVarID;
    
    public IndexPathVariableEntry(String pathVarName, String pathVarPath, int id){
        this.pathVariableName = pathVarName;
        this.pathVariablePath = pathVarPath;
        this.pathVarID	= id;
    }

    /**
     * @return Returns the id.
     */
    public int getId() {
        return pathVarID;
    }
    /**
     * @param id The id to set.
     */
    public void setId(int id) {
        this.pathVarID = id;
    }
    /**
     * @return Returns the pathVariableName.
     */
    public String getPathVariableName() {
        return pathVariableName;
    }
    /**
     * @return Returns the pathVariablePath.
     */
    public String getPathVariablePath() {
        return pathVariablePath;
    }
    
	/**
	 * Returns the size of the indexedFile.
	 */
	public int footprint() {
		//object+ 3 slots + size of the string (Object size + (4 fields in String class)
	    //+ 8 for char array in string + (2 for each char * number of chars in string)) + {another String}
		return 8 + (3 * 4) + (8 + (4 * 4) + 8 + (pathVariableName.length() * 2)) + (8 + (4 * 4) + 8 + (pathVariablePath.length() * 2));
	}
	
	public String toString() {
		return "IndexPathVariableEntry(" + pathVarID + ": " + pathVariableName + " + " + pathVariablePath + ")"; //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	
}
