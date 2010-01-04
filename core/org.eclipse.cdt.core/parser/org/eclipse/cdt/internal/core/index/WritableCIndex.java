/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.internal.core.pdom.ASTFilePathResolver;
import org.eclipse.core.runtime.CoreException;

public class WritableCIndex extends CIndex implements IWritableIndex {

	final private IWritableIndexFragment fWritableFragment;
	private boolean fIsWriteLocked= false;

	public WritableCIndex(IWritableIndexFragment writable, IIndexFragment[] readonly) {
		super (concat(writable, readonly));
		fWritableFragment= writable;
	}

	private static IIndexFragment[] concat(IIndexFragment writable, IIndexFragment[] readonly) {
		IIndexFragment[] result= new IIndexFragment[1 + readonly.length];
		result[0]= writable;
		System.arraycopy(readonly, 0, result, 1, readonly.length);
		return result;
	}

	public IWritableIndexFragment getWritableFragment() {
		return fWritableFragment;
	}
	
	public IIndexFragmentFile getWritableFile(int linkageID, IIndexFileLocation location) throws CoreException {
		return fWritableFragment.getFile(linkageID, location);
	}
	
	public IIndexFragmentFile[] getWritableFiles(IIndexFileLocation location) throws CoreException {
		return fWritableFragment.getFiles(location);
	}

	public IIndexFragmentFile addFile(int linkageID, IIndexFileLocation fileLocation) throws CoreException {
		return fWritableFragment.addFile(linkageID, fileLocation);
	}

	private boolean isWritableFragment(IIndexFragment frag) {
		return frag == fWritableFragment;
	}

	public void setFileContent(IIndexFragmentFile file, int linkageID,
			IncludeInformation[] includes,
			IASTPreprocessorStatement[] macros, IASTName[][] names, ASTFilePathResolver resolver) throws CoreException {
		IIndexFragment indexFragment = file.getIndexFragment();
		if (!isWritableFragment(indexFragment)) {
			assert false : "Attempt to update file of read-only fragment"; //$NON-NLS-1$
		} else {
			for (IncludeInformation ii : includes) {
				if (ii.fLocation != null) {
					ii.fTargetFile= addFile(linkageID, ii.fLocation);
				}
			}
			((IWritableIndexFragment) indexFragment).addFileContent(file, includes, macros, names, resolver);
		}
	}

	public void clear() throws CoreException {
		fWritableFragment.clear();
	}

	public boolean isWritableFile(IIndexFile file) {
		return file instanceof IIndexFragmentFile && 
				isWritableFragment(((IIndexFragmentFile)file).getIndexFragment());
	}
	
	public void clearFile(IIndexFragmentFile file, Collection<IIndexFileLocation> clearedContexts) throws CoreException {
		IIndexFragment indexFragment = file.getIndexFragment();
		if (!isWritableFragment(indexFragment)) {
			assert false : "Attempt to clear file of read-only fragment"; //$NON-NLS-1$
		} else {
			((IWritableIndexFragment) indexFragment).clearFile(file, clearedContexts);
		}
	}

	@Override
	public synchronized void acquireReadLock() throws InterruptedException {
		assert !fIsWriteLocked: "Read locks are not allowed while write-locked."; //$NON-NLS-1$
		super.acquireReadLock();
	}

	@Override
	public synchronized void releaseReadLock() {
		assert !fIsWriteLocked: "Read locks are not allowed while write-locked."; //$NON-NLS-1$
		super.releaseReadLock();
	}

	public synchronized void acquireWriteLock(int giveupReadlockCount) throws InterruptedException {
		assert !fIsWriteLocked: "Multiple write locks is not allowed"; //$NON-NLS-1$
		assert giveupReadlockCount == getReadLockCount(): "Unexpected read lock is not allowed"; //$NON-NLS-1$
		
		fWritableFragment.acquireWriteLock(giveupReadlockCount);
		fIsWriteLocked= true;
	}

	public synchronized void releaseWriteLock(int establishReadlockCount) {
		releaseWriteLock(establishReadlockCount, true);
	}

	public synchronized void releaseWriteLock(int establishReadlockCount, boolean flush) {
		assert fIsWriteLocked: "No write lock to be released"; //$NON-NLS-1$
		assert establishReadlockCount == getReadLockCount(): "Unexpected read lock is not allowed"; //$NON-NLS-1$

		fIsWriteLocked= false;
		fWritableFragment.releaseWriteLock(establishReadlockCount, flush);
	}
	

	public void flush() throws CoreException {
		assert !fIsWriteLocked;
		fWritableFragment.flush();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.index.IWritableIndex#getDatabaseSizeBytes()
	 */
	public long getDatabaseSizeBytes() {
		return fWritableFragment.getDatabaseSizeBytes();
	}
}
