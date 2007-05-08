/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.index;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.internal.core.index.IWritableIndex.IncludeInformation;
import org.eclipse.core.runtime.CoreException;

/**
 * The interface that an actual storage for an index has to implement.
 */
public interface IWritableIndexFragment extends IIndexFragment {

	/**
	 * Clears the entire fragment.
	 */
	void clear() throws CoreException;

	/**
	 * Clears the given file in the index.
	 * @param file a file to clear, must belong to this fragment.
	 * @param a collection that receives IndexFileLocation objects for files that
	 *     had the cleared file as a context.
	 */
	void clearFile(IIndexFragmentFile file, Collection contextsRemoved) throws CoreException;

	/**
	 * Creates a file object for the given location or returns an existing one.
	 * @param fileLocation an IIndexFileLocation representing the location of the file
	 * @return the existing IIndexFragmentFile for this location, or a newly created one 
	 * @throws CoreException
	 */
	IIndexFragmentFile addFile(IIndexFileLocation fileLocation) throws CoreException;

	/**
	 * Adds an include to the given file.
	 */
	void addFileContent(IIndexFragmentFile sourceFile, 
			IncludeInformation[] includes,  
			IASTPreprocessorMacroDefinition[] macros, IASTName[][] names) throws CoreException;

	/**
	 * Acquires a write lock, while giving up a certain amount of read locks.
	 */
	void acquireWriteLock(int giveupReadLockCount) throws InterruptedException;
	
	/**
	 * Releases a write lock, reestablishing a certain amount of read locks.
	 * @param establishReadLockCount amount of read-locks to establish
	 * @param flush if <code>true</code> changes are flushed to disk
	 */
	void releaseWriteLock(int establishReadLockCount, boolean flush);
	
	/**
	 * Write the key, value mapping to the fragment properties. If a mapping for the
	 * same key already exists, it is overwritten.
	 * @param key a non-null property name
	 * @param value a value to associate with the key. may not be null.
	 * @throws CoreException
	 * @throws NullPointerException if key is null
	 */
	public void setProperty(String propertyName, String value) throws CoreException;

	/**
	 * Flushes caches to disk.
	 */
	void flush() throws CoreException;
}
