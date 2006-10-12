/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.core.runtime.CoreException;

/**
 * Interface used by the indexer to write to the index. 
 *
 * @since 4.0
 */
public interface IWritableIndex extends IIndex {

	/**
	 * Creates a file object for the given location or returns an existing one.
	 */
	IIndexFragmentFile addFile(String fileLocation) throws CoreException;

	/**
	 * Adds an AST name to the given file.
	 */
	void addName(IIndexFragmentFile sourceFile, IASTName name) throws CoreException;

	/**
	 * Adds a AST macro to the given file.
	 */
	void addMacro(IIndexFragmentFile sourceFile, IASTPreprocessorMacroDefinition macro) throws CoreException;

	/**
	 * Adds an include to the given file.
	 */
	void addInclude(IIndexFragmentFile sourceFile, IIndexFragmentFile destFile) throws CoreException;

	/**
	 * Clears the entire index.
	 */
	void clear() throws CoreException;

	/**
	 * Clears the given file in the index.
	 */
	void clearFile(IIndexFragmentFile file) throws CoreException;

	/**
	 * Acquires a write lock, while giving up a certain amount of read locks.
	 */
	void acquireWriteLock(int giveupReadLockCount) throws InterruptedException;

	/**
	 * Releases a write lock, reestablishing a certain amount of read locks.
	 */
	void releaseWriteLock(int establishReadLockCount);
}
