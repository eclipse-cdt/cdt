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
import org.eclipse.core.runtime.CoreException;

public class WritableCIndex extends CIndex implements IWritableIndex {

	final private IWritableIndexFragment[] fWritableFragments;
	private boolean fIsWriteLocked= false;

	public WritableCIndex(IWritableIndexFragment[] writable, IIndexFragment[] readonly) {
		super (concat(writable, readonly));
		fWritableFragments= writable;
	}

	private static IIndexFragment[] concat(IIndexFragment[] writable, IIndexFragment[] readonly) {
		IIndexFragment[] result= new IIndexFragment[writable.length + readonly.length];
		System.arraycopy(writable, 0, result, 0, writable.length);
		System.arraycopy(readonly, 0, result, writable.length, readonly.length);
		return result;
	}

	public IIndexFragmentFile addFile(IIndexFileLocation fileLocation) throws CoreException {
		IWritableIndexFragment frag= selectFragment(fileLocation);
		return frag.addFile(fileLocation);
	}

	private IWritableIndexFragment selectFragment(IIndexFileLocation fileLocation) {
		// todo handling of multiple writable indices
		assert fWritableFragments.length == 1;
		return fWritableFragments[0];
	}

	private boolean isWritableFragment(IIndexFragment frag) {
		for (int i = 0; i < fWritableFragments.length; i++) {
			if (fWritableFragments[i] == frag) {
				return true;
			}
		}
		return false;
	}

	public void setFileContent(IIndexFragmentFile file, 
			IncludeInformation[] includes,
			IASTPreprocessorMacroDefinition[] macros, IASTName[][] names) throws CoreException {

		IIndexFragment indexFragment = file.getIndexFragment();
		if (!isWritableFragment(indexFragment)) {
			assert false : "Attempt to update file of read-only fragment"; //$NON-NLS-1$
		}
		else {
			for (int i = 0; i < includes.length; i++) {
				IncludeInformation ii= includes[i];
				if (ii.fLocation != null) {
					ii.fTargetFile= addFile(ii.fLocation);
				}
			}
			((IWritableIndexFragment) indexFragment).addFileContent(file, includes, macros, names);
		}
	}

	public void clear() throws CoreException {
		for (int i = 0; i < fWritableFragments.length; i++) {
			IWritableIndexFragment frag = fWritableFragments[i];
			frag.clear();
		}
	}

	public boolean isWritableFile(IIndexFragmentFile file) {
		return isWritableFragment(file.getIndexFragment());
	}
	
	public void clearFile(IIndexFragmentFile file, Collection clearedContexts) throws CoreException {
		IIndexFragment indexFragment = file.getIndexFragment();
		if (!isWritableFragment(indexFragment)) {
			assert false : "Attempt to clear file of read-only fragment"; //$NON-NLS-1$
		}
		else {
			((IWritableIndexFragment) indexFragment).clearFile(file, clearedContexts);
		}
	}

	
	public synchronized void acquireReadLock() throws InterruptedException {
		assert !fIsWriteLocked: "Read locks are not allowed while write-locked."; //$NON-NLS-1$
		super.acquireReadLock();
	}

	public synchronized void releaseReadLock() {
		assert !fIsWriteLocked: "Read locks are not allowed while write-locked."; //$NON-NLS-1$
		super.releaseReadLock();
	}

	public synchronized void acquireWriteLock(int giveupReadlockCount) throws InterruptedException {
		assert !fIsWriteLocked: "Multiple write locks is not allowed"; //$NON-NLS-1$
		assert giveupReadlockCount == getReadLockCount(): "Unexpected read lock is not allowed"; //$NON-NLS-1$
		
		fIsWriteLocked= true;
		int i= 0;
		try {
			for (i = 0; i < fWritableFragments.length; i++) {
				fWritableFragments[i].acquireWriteLock(giveupReadlockCount);
			}
		}
		finally {
			if (i < fWritableFragments.length) {
				// rollback
				fIsWriteLocked= false;
				while (--i >= 0) {
					fWritableFragments[i].releaseWriteLock(giveupReadlockCount, false);
				}
			}
		}
	}

	public synchronized void releaseWriteLock(int establishReadlockCount) {
		releaseWriteLock(establishReadlockCount, true);
	}

	public synchronized void releaseWriteLock(int establishReadlockCount, boolean flush) {
		assert fIsWriteLocked: "No write lock to be released"; //$NON-NLS-1$
		assert establishReadlockCount == getReadLockCount(): "Unexpected read lock is not allowed"; //$NON-NLS-1$

		fIsWriteLocked= false;
		for (int i = 0; i < fWritableFragments.length; i++) {
			fWritableFragments[i].releaseWriteLock(establishReadlockCount, flush);
		}
	}
	
	public IWritableIndexFragment getPrimaryWritableFragment() {
		return fWritableFragments.length > 0 ? fWritableFragments[0] : null;
	}

	public void flush() throws CoreException {
		assert !fIsWriteLocked;
		int i= 0;
		for (i = 0; i < fWritableFragments.length; i++) {
			fWritableFragments[i].flush();
		}
	}
	
	
}
