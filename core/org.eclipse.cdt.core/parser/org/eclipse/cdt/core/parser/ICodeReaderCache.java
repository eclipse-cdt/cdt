/*******************************************************************************
 * Copyright (c) 2005 Rational Software Corporation and others. All rights
 * reserved. This program and the accompanying materials are made available
 * under the terms of the Common Public License v0.5 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: Rational Software - Initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.core.parser;

/**
 * This is the interface to a cache for CodeReaders.
 * 
 * For thread safety the implementations of this interface must ensure that their methods are thread safe. 
 * 
 * @author dsteffle
 */
public interface ICodeReaderCache {

	/**
	 * Retrieves the CodeReader corresponding to the key specified that represents the 
	 * path for that CodeReader.  If no CodeReader is found in the cache then a new CodeReader
	 * is created for the path and then returned.
	 * 
	 * @param key the path corresponding to the CodeReader, generally: 
	 * fileToParse.getLocation().toOSString()
	 * @return the CodeReader corresponding to the path specified by the key
	 */
	public CodeReader get(String key);
	
	/**
	 * Used to remove the CodeReader corresponding to the path specified by key from the cache.
	 * 
	 * @param key the path of the CodeReader to be removed
	 * @return the removed CodeReader or null if not found
	 */
	public CodeReader remove(String key);

	/**
	 * Returns the amount of space that the cache is using.
	 * The units are relative to the implementation of the cache.  It could be
	 * the total number of objects in the cache, or the total space the cache is 
	 * using in MB.
	 * 
	 * @return
	 */
	public int getCurrentSpace();
}
