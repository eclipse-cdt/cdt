/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core.scannerconfig;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class PathInfo {
	private static final Path[] EMPTY_PATH_ARRAY = new Path[0];
	public final static PathInfo EMPTY_INFO = new PathInfo(null, null, null, null, null);

	private static int EMPTY_CODE = 53;
	
	private IPath[] fIncludePaths;
	private IPath[] fQuoteIncludePaths;
	private HashMap<String, String> fSymbols;
	private IPath[] fIncludeFiles;
	private IPath[] fMacroFiles;
	private int fHash;
	
	public PathInfo(IPath[] includePaths,
			IPath[] quoteIncludePaths,
			Map<String, String> symbols,
			IPath[] includeFiles,
			IPath[] macroFiles){
		fIncludePaths = includePaths != null && includePaths.length != 0 ? (IPath[])includePaths.clone() : EMPTY_PATH_ARRAY;
		fQuoteIncludePaths = quoteIncludePaths != null && quoteIncludePaths.length != 0 ? (IPath[])quoteIncludePaths.clone() : EMPTY_PATH_ARRAY;
		fSymbols = symbols != null && symbols.size() != 0 ? new HashMap<String, String>(symbols) : new HashMap<String, String>(0);
		fIncludeFiles = includeFiles != null && includeFiles.length != 0 ? (IPath[])includeFiles.clone() : EMPTY_PATH_ARRAY;
		fMacroFiles = macroFiles != null && macroFiles.length != 0 ? (IPath[])macroFiles.clone() : EMPTY_PATH_ARRAY;
	}

    /**
     * Get include paths  
     */
    public IPath[] getIncludePaths(){
    	return fIncludePaths.length != 0 ? (IPath[])fIncludePaths.clone() : EMPTY_PATH_ARRAY;
    }
    /**
     * Get quote include paths (for #include "...") 
     */
    public IPath[] getQuoteIncludePaths(){
    	return fQuoteIncludePaths.length != 0 ? (IPath[])fQuoteIncludePaths.clone() : EMPTY_PATH_ARRAY;
    }
    /**
     * Get defined symbols  
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getSymbols(){
        return (Map<String, String>)fSymbols.clone();
    }
    
    /**
     * Get include files (gcc option -include)
     */
    public IPath[] getIncludeFiles(){
    	return fIncludeFiles.length != 0 ? (IPath[])fIncludeFiles.clone() : EMPTY_PATH_ARRAY;
    }
    /**
     * Get macro files (gcc option -imacros) 
     */
    public IPath[] getMacroFiles(){
    	return fMacroFiles.length != 0 ? (IPath[])fMacroFiles.clone() : EMPTY_PATH_ARRAY;
    }
	/**
	 * Returns if there is any discovered scanner info
	 */
	public boolean isEmpty(){
		return fIncludePaths.length == 0
			&& fQuoteIncludePaths.length == 0
			&& fSymbols.size() == 0
			&& fIncludeFiles.length == 0
			&& fMacroFiles.length == 0;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		
		if(!(obj instanceof PathInfo))
			return false;
		
		PathInfo other = (PathInfo)obj;
		
		if(!Arrays.equals(fIncludePaths, other.fIncludePaths))
			return false;
		if(!Arrays.equals(fQuoteIncludePaths, other.fQuoteIncludePaths))
			return false;
		if(!fSymbols.equals(other.fSymbols))
			return false;
		if(!Arrays.equals(fIncludeFiles, other.fIncludeFiles))
			return false;
		if(!Arrays.equals(fMacroFiles, other.fMacroFiles))
			return false;
		
		return true;
	}

	@Override
	public int hashCode() {
		int hash = fHash;
		if(hash == 0){
			hash = EMPTY_CODE;
			
			if(fIncludePaths.length != 0){
				for(int i = 0; i < fIncludePaths.length; i++){
					hash += fIncludePaths[i].hashCode();
				}
			}

			if(fQuoteIncludePaths.length != 0){
				for(int i = 0; i < fQuoteIncludePaths.length; i++){
					hash += fQuoteIncludePaths[i].hashCode();
				}
			}
			
			hash += fSymbols.hashCode();
			
			if(fIncludeFiles.length != 0){
				for(int i = 0; i < fIncludeFiles.length; i++){
					hash += fIncludeFiles[i].hashCode();
				}
			}
			
			if(fMacroFiles.length != 0){
				for(int i = 0; i < fMacroFiles.length; i++){
					hash += fMacroFiles[i].hashCode();
				}
			}

			fHash = hash;
		}
		return hash;
	}
}
