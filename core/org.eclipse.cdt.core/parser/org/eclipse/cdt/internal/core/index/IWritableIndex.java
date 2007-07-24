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
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.core.runtime.CoreException;

/**
 * Interface used by the indexer to write to the index. 
 *
 * @since 4.0
 */
public interface IWritableIndex extends IIndex {
	
	static class IncludeInformation {
		public IASTPreprocessorIncludeStatement fStatement;
		public IIndexFileLocation fLocation;
		public IIndexFragmentFile fTargetFile;
		public boolean fIsContext= false;
	}
	
	/**
	 * Checks whether the given file can be written to in this index.
	 */
	boolean isWritableFile(IIndexFragmentFile file);

	/**
	 * Clears the given file in the index.
	 * @param file a file to clear.
	 * @param a collection that receives IndexFileLocation objects for files that
	 *     had the cleared file as a context. May be <code>null</code>.
	 */
	void clearFile(IIndexFragmentFile file, Collection clearedContexts) throws CoreException;

	/**
	 * Creates a file object for the given location or returns an existing one.
	 */
	IIndexFragmentFile addFile(IIndexFileLocation fileLocation) throws CoreException;

	/**
	 * Adds content to the given file.
	 */
	void setFileContent(IIndexFragmentFile sourceFile, 
			IncludeInformation[] includes, 
			IASTPreprocessorMacroDefinition[] macros, IASTName[][] names) throws CoreException;

	/**
	 * Clears the entire index.
	 */
	void clear() throws CoreException;

	/**
	 * Acquires a write lock, while giving up a certain amount of read locks.
	 */
	void acquireWriteLock(int giveupReadLockCount) throws InterruptedException;

	/**
	 * Releases a write lock, reestablishing a certain amount of read locks.
	 * Fully equivalent to <code>releaseWriteLock(int, true)</code>.
	 */
	void releaseWriteLock(int establishReadLockCount);

	/**
	 * Releases a write lock, reestablishing a certain amount of read locks.
	 * @param establishReadLockCount amount of read-locks to establish.
	 * @param flushDatabase when true the changes are flushed to disk.
	 */
	void releaseWriteLock(int establishReadLockCount, boolean flushDatabase);
	
	/**
	 * Resets the counters for cache-hits
	 */
	void resetCacheCounters();
	
	/**
	 * Returns cache hits since last reset of counters.
	 */
	long getCacheHits();
	
	/**
	 * Returns cache misses since last reset of counters.
	 */
	long getCacheMisses();

	/**
	 * Returns the primary writable fragment, or <code>null</code> if there is 
	 * no writable fragment.
	 */
	IWritableIndexFragment getPrimaryWritableFragment();

	/**
	 * Flushes all caches to the disk. 
	 */
	void flush() throws CoreException;
}
