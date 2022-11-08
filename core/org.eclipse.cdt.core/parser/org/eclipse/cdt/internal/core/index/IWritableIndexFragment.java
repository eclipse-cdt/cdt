/*******************************************************************************
 * Copyright (c) 2006, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.ISignificantMacros;
import org.eclipse.cdt.internal.core.index.IWritableIndex.IncludeInformation;
import org.eclipse.cdt.internal.core.pdom.ASTFilePathResolver;
import org.eclipse.cdt.internal.core.pdom.YieldableIndexLock;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

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
	void clearFile(IIndexFragmentFile file) throws CoreException;

	/**
	 * Creates a file object for the given location and linkage or returns an existing one.
	 * @param fileLocation an IIndexFileLocation representing the location of the file.
	 * @param sigMacros the macro definitions at the inclusion point.
	 * @return the existing IIndexFragmentFile for this location, or a newly created one.
	 * @throws CoreException
	 */
	IIndexFragmentFile addFile(int linkageID, IIndexFileLocation fileLocation, ISignificantMacros sigMacros)
			throws CoreException;

	/**
	 * Creates a file object for the given location and linkage. The created file object is not added to
	 * the file index.
	 * @param fileLocation an IIndexFileLocation representing the location of the file.
	 * @param sigMacros the macro definitions at the inclusion point.
	 * @return a newly created IIndexFragmentFile.
	 * @throws CoreException
	 */
	IIndexFragmentFile addUncommittedFile(int linkageID, IIndexFileLocation fileLocation, ISignificantMacros sigMacros)
			throws CoreException;

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
	 * Adds includes, macros and names to the given file.
	 */
	void addFileContent(IIndexFragmentFile sourceFile, IncludeInformation[] includes,
			IASTPreprocessorStatement[] macros, IASTName[][] names, ASTFilePathResolver resolver,
			YieldableIndexLock lock) throws CoreException, InterruptedException;

	/**
	 * Acquires a write lock, while giving up a certain amount of read locks.
	 */
	void acquireWriteLock(int giveupReadLockCount, IProgressMonitor monitor) throws InterruptedException;

	/**
	 * Releases a write lock, reestablishing a certain amount of read locks.
	 * @param establishReadLockCount amount of read-locks to establish
	 * @param flush if <code>true</code> changes are flushed to disk
	 */
	void releaseWriteLock(int establishReadLockCount, boolean flush);

	/**
	 * Write the key, value mapping to the fragment properties. If a mapping for the
	 * same key already exists, it is overwritten.
	 * @param propertyName a non-null property name
	 * @param value a value to associate with the key. may not be null.
	 * @throws CoreException
	 * @throws NullPointerException if key is null
	 */
	public void setProperty(String propertyName, String value) throws CoreException;

	/**
	 * Flushes caches to disk.
	 */
	void flush() throws CoreException;

	/**
	 * @return the size of the database in bytes
	 */
	long getDatabaseSizeBytes();
}
