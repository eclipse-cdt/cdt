/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.ISignificantMacros;
import org.eclipse.cdt.internal.core.pdom.ASTFilePathResolver;
import org.eclipse.cdt.internal.core.pdom.YieldableIndexLock;
import org.eclipse.core.runtime.CoreException;

/**
 * Interface used by the indexer to write to the index. A writable index is not thread-safe,
 * each instance must not be used within more than one thread.
 *
 * @since 4.0
 */
public interface IWritableIndex extends IIndex {
	
	static class IncludeInformation {
		public final IASTPreprocessorIncludeStatement fStatement;
		public final IIndexFileLocation fLocation;
		public final ISignificantMacros fSignificantMacros;
		public final boolean fIsContext;
		public IIndexFragmentFile fTargetFile;
		
		public IncludeInformation(IASTPreprocessorIncludeStatement stmt, 
				IIndexFileLocation location, ISignificantMacros sig, boolean isContext) {
			fStatement= stmt;
			fSignificantMacros= sig;
			fLocation= location;
			fIsContext= isContext;
		}
	}
	
	/**
	 * Checks whether the given file can be written to in this index.
	 */
	boolean isWritableFile(IIndexFile file);

	/**
	 * Returns a writable file for the given location, linkage, and the set of macro definitions,
	 * or null. This method returns file objects without content, also.
	 */
	IIndexFragmentFile getWritableFile(int linkageID, IIndexFileLocation location,
			ISignificantMacros macroDictionary) throws CoreException;

	/**
	 * Returns the writable files for the given location and linkage. This method
	 * returns file objects without content, also.
	 */
	IIndexFragmentFile[] getWritableFiles(int linkageID, IIndexFileLocation location) throws CoreException;

	/**
	 * Returns the writable files for the given location in any linkage. This method
	 * returns file objects without content, also.
	 */
	IIndexFragmentFile[] getWritableFiles(IIndexFileLocation location) throws CoreException;

	/**
	 * Clears the given file in the index.
	 * @param file a file to clear.
	 * @param a collection that receives IndexFileLocation objects for files that
	 *     had the cleared file as a context. May be <code>null</code>.
	 */
	void clearFile(IIndexFragmentFile file) throws CoreException;

	/**
	 * Creates a file object for the given location or returns an existing one.
	 * @param linkageID the id of the linkage in which the file has been parsed.
	 * @param location the IIndexFileLocation representing the location of the file
	 * @param macroDictionary The names and definitions of the macros used to disambiguate between
	 *     variants of the file contents corresponding to different inclusion points.
	 * @return A created or an existing file.  
	 */
	IIndexFragmentFile addFile(int linkageID, IIndexFileLocation location,
			ISignificantMacros macroDictionary) throws CoreException;

	/**
	 * Creates a uncommitted file object for the given location.
	 */
	IIndexFragmentFile addUncommittedFile(int linkageID, IIndexFileLocation location,
			ISignificantMacros macroDictionary) throws CoreException;

	/**
	 * Makes an uncommitted file that was created earlier by calling
	 * {@link #addUncommittedFile(int, IIndexFileLocation, ISignificantMacros)} method visible in the index.
	 *
	 * @return The file that was updated.
	 * @throws CoreException
	 */
	IIndexFragmentFile commitUncommittedFile() throws CoreException;

	/**
	 * Removes an uncommitted file if there is one. Used to recover from a failed index update.
	 *  
	 * @throws CoreException
	 */
	void clearUncommittedFile() throws CoreException;

	/**
	 * Adds content to the given file.
	 */
	void setFileContent(IIndexFragmentFile sourceFile, 
			int linkageID, IncludeInformation[] includes, 
			IASTPreprocessorStatement[] macros, IASTName[][] names,
			ASTFilePathResolver resolver, YieldableIndexLock lock) throws CoreException, InterruptedException;

	/**
	 * Clears the entire index.
	 */
	void clear() throws CoreException;

	/**
	 * Acquires a write lock, while giving up a certain amount of read locks.
	 */
	void acquireWriteLock() throws InterruptedException;

	/**
	 * Releases a write lock, reestablishing a certain amount of read locks.
	 * Fully equivalent to <code>releaseWriteLock(int, true)</code>.
	 */
	void releaseWriteLock();

	/**
	 * Releases a write lock, reestablishing a certain amount of read locks.
	 * @param establishReadLockCount amount of read-locks to establish.
	 * @param flushDatabase when true the changes are flushed to disk.
	 */
	void releaseWriteLock(boolean flushDatabase);
	
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
	IWritableIndexFragment getWritableFragment();

	/**
	 * Flushes all caches to the disk. 
	 */
	void flush() throws CoreException;

	/**
	 * Returns the size of the database in bytes.
	 */
	long getDatabaseSizeBytes();

	/**
	 * Clears the result cache, caller needs to hold a write-lock.
	 */
	void clearResultCache();

	/**
	 * Changes the inclusions pointing to 'source' to point to 'target', instead. 
	 * Both files must belong to the writable fragment.
	 */
	void transferIncluders(IIndexFragmentFile source, IIndexFragmentFile target) throws CoreException;

	/**
	 * Changes the inclusion from the context of 'source' to point to 'target', instead. 
	 * Both files must belong to the writable fragment.
	 */
	void transferContext(IIndexFragmentFile source, IIndexFragmentFile target) throws CoreException;
}
