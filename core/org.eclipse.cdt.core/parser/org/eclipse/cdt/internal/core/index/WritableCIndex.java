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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.ISignificantMacros;
import org.eclipse.cdt.internal.core.pdom.ASTFilePathResolver;
import org.eclipse.cdt.internal.core.pdom.YieldableIndexLock;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class WritableCIndex extends CIndex implements IWritableIndex {
	private boolean fIsWriteLocked;
	private Object fThread;

	public WritableCIndex(IWritableIndexFragment writable) {
		super(new IWritableIndexFragment[] { writable });
	}

	@Override
	public IWritableIndexFragment getWritableFragment() {
		return (IWritableIndexFragment) getFragments()[0];
	}

	@Override
	public IIndexFragmentFile getWritableFile(int linkageID, IIndexFileLocation location,
			ISignificantMacros macroDictionary) throws CoreException {
		return (IIndexFragmentFile) super.getFile(linkageID, location, macroDictionary);
	}

	@Override
	public IIndexFragmentFile[] getWritableFiles(int linkageID, IIndexFileLocation location) throws CoreException {
		return getWritableFragment().getFiles(linkageID, location);
	}

	@Override
	public IIndexFragmentFile[] getWritableFiles(IIndexFileLocation location) throws CoreException {
		return getWritableFragment().getFiles(location);
	}

	@Override
	public IIndexFragmentFile addFile(int linkageID, IIndexFileLocation location, ISignificantMacros macroDictionary)
			throws CoreException {
		return getWritableFragment().addFile(linkageID, location, macroDictionary);
	}

	@Override
	public IIndexFragmentFile addUncommittedFile(int linkageID, IIndexFileLocation location,
			ISignificantMacros macroDictionary) throws CoreException {
		return getWritableFragment().addUncommittedFile(linkageID, location, macroDictionary);
	}

	@Override
	public IIndexFragmentFile commitUncommittedFile() throws CoreException {
		return getWritableFragment().commitUncommittedFile();
	}

	@Override
	public void clearUncommittedFile() throws CoreException {
		getWritableFragment().clearUncommittedFile();
	}

	@Override
	public void setFileContent(IIndexFragmentFile file, int linkageID, IncludeInformation[] includes,
			IASTPreprocessorStatement[] macros, IASTName[][] names, ASTFilePathResolver resolver,
			YieldableIndexLock lock) throws CoreException, InterruptedException {
		assert getWritableFragment() == file.getIndexFragment();

		for (IncludeInformation include : includes) {
			if (include.fLocation != null) {
				include.fTargetFile = addFile(linkageID, include.fLocation, include.fSignificantMacros);
			}
		}
		getWritableFragment().addFileContent(file, includes, macros, names, resolver, lock);
	}

	@Override
	public void clear() throws CoreException {
		getWritableFragment().clear();
	}

	@Override
	public void clearFile(IIndexFragmentFile file) throws CoreException {
		getWritableFragment().clearFile(file);
	}

	@Override
	public void acquireReadLock() throws InterruptedException {
		checkThread();
		assert !fIsWriteLocked : "Read locks are not allowed while write-locked."; //$NON-NLS-1$
		super.acquireReadLock();
	}

	@Override
	public void releaseReadLock() {
		checkThread();
		assert !fIsWriteLocked : "Read locks are not allowed while write-locked."; //$NON-NLS-1$
		super.releaseReadLock();
		if (getReadLockCount() == 0)
			fThread = null;
	}

	@Override
	public void acquireWriteLock(IProgressMonitor monitor) throws InterruptedException {
		checkThread();
		assert !fIsWriteLocked : "Multiple write locks is not allowed"; //$NON-NLS-1$

		getWritableFragment().acquireWriteLock(getReadLockCount(), monitor);
		fIsWriteLocked = true;
	}

	@Override
	public void releaseWriteLock() {
		releaseWriteLock(true);
	}

	@Override
	public void releaseWriteLock(boolean flush) {
		checkThread();
		assert fIsWriteLocked : "No write lock to be released"; //$NON-NLS-1$

		// Bug 297641: Result cache of read only providers needs to be cleared.
		int establishReadlockCount = getReadLockCount();
		if (establishReadlockCount == 0) {
			clearResultCache();
		}

		fIsWriteLocked = false;
		getWritableFragment().releaseWriteLock(establishReadlockCount, flush);

		if (establishReadlockCount == 0) {
			fThread = null;
		}
	}

	private void checkThread() {
		if (fThread == null) {
			fThread = Thread.currentThread();
		} else if (fThread != Thread.currentThread()) {
			throw new IllegalArgumentException("A writable index must not be used from multiple threads."); //$NON-NLS-1$
		}
	}

	@Override
	public void clearResultCache() {
		assert fIsWriteLocked : "Need to hold a write lock to clear result caches"; //$NON-NLS-1$
		super.clearResultCache();
	}

	@Override
	public void flush() throws CoreException {
		assert !fIsWriteLocked;
		getWritableFragment().flush();
	}

	@Override
	public long getDatabaseSizeBytes() {
		return getWritableFragment().getDatabaseSizeBytes();
	}

	@Override
	public void transferIncluders(IIndexFragmentFile source, IIndexFragmentFile target) throws CoreException {
		if (source == null || target == null)
			throw new IllegalArgumentException();
		if (source.equals(target))
			return;
		target.transferIncluders(source);
	}

	@Override
	public void transferContext(IIndexFragmentFile source, IIndexFragmentFile target) throws CoreException {
		if (source == null || target == null)
			throw new IllegalArgumentException();
		if (source.equals(target))
			return;
		target.transferContext(source);
	}
}
