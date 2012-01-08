/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Andrew Ferguson (Symbian)
 *    Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.ISignificantMacros;
import org.eclipse.cdt.internal.core.pdom.ASTFilePathResolver;
import org.eclipse.cdt.internal.core.pdom.YieldableIndexLock;
import org.eclipse.core.runtime.CoreException;

public class WritableCIndex extends CIndex implements IWritableIndex {
	final private IWritableIndexFragment fWritableFragment;
	private boolean fIsWriteLocked= false;
	private Object fThread;

	public WritableCIndex(IWritableIndexFragment writable, IIndexFragment[] readonly) {
		super(concat(writable, readonly));
		fWritableFragment= writable;
	}

	private static IIndexFragment[] concat(IIndexFragment writable, IIndexFragment[] readonly) {
		IIndexFragment[] result= new IIndexFragment[1 + readonly.length];
		result[0]= writable;
		System.arraycopy(readonly, 0, result, 1, readonly.length);
		return result;
	}

	@Override
	public IWritableIndexFragment getWritableFragment() {
		return fWritableFragment;
	}
	
	@Override
	public IIndexFragmentFile getWritableFile(int linkageID, IIndexFileLocation location,
			ISignificantMacros macroDictionary) throws CoreException {
		return fWritableFragment.getFile(linkageID, location, macroDictionary);
	}

	@Override
	public IIndexFragmentFile[] getWritableFiles(int linkageID, IIndexFileLocation location) throws CoreException {
		return fWritableFragment.getFiles(linkageID, location);
	}

	@Override
	public IIndexFragmentFile[] getWritableFiles(IIndexFileLocation location) throws CoreException {
		return fWritableFragment.getFiles(location);
	}

	@Override
	public IIndexFragmentFile addFile(int linkageID, IIndexFileLocation location,
			ISignificantMacros macroDictionary) throws CoreException {
		return fWritableFragment.addFile(linkageID, location, macroDictionary);
	}

	@Override
	public IIndexFragmentFile addUncommittedFile(int linkageID, IIndexFileLocation location,
			ISignificantMacros macroDictionary) throws CoreException {
		return fWritableFragment.addUncommittedFile(linkageID, location, macroDictionary);
	}

	@Override
	public IIndexFragmentFile commitUncommittedFile() throws CoreException {
		return fWritableFragment.commitUncommittedFile();
	}

	@Override
	public void clearUncommittedFile() throws CoreException {
		fWritableFragment.clearUncommittedFile();
	}

	private boolean isWritableFragment(IIndexFragment frag) {
		return frag == fWritableFragment;
	}

	@Override
	public void setFileContent(IIndexFragmentFile file, int linkageID, IncludeInformation[] includes,
			IASTPreprocessorStatement[] macros, IASTName[][] names, ASTFilePathResolver resolver,
			YieldableIndexLock lock) throws CoreException, InterruptedException {
		IIndexFragment indexFragment = file.getIndexFragment();
		if (!isWritableFragment(indexFragment)) {
			assert false : "Attempt to update file of read-only fragment"; //$NON-NLS-1$
		} else {
			for (IncludeInformation include : includes) {
				if (include.fLocation != null) {
					include.fTargetFile= addFile(linkageID, include.fLocation,
							include.fSignificantMacros);
				}
			}
			((IWritableIndexFragment) indexFragment).addFileContent(file, includes, macros, names, resolver, lock);
		}
	}

	@Override
	public void clear() throws CoreException {
		fWritableFragment.clear();
	}

	@Override
	public boolean isWritableFile(IIndexFile file) {
		return file instanceof IIndexFragmentFile && 
				isWritableFragment(((IIndexFragmentFile)file).getIndexFragment());
	}
	
	@Override
	public void clearFile(IIndexFragmentFile file) throws CoreException {
		IIndexFragment indexFragment = file.getIndexFragment();
		if (!isWritableFragment(indexFragment)) {
			assert false : "Attempt to clear file of read-only fragment"; //$NON-NLS-1$
		} else {
			((IWritableIndexFragment) indexFragment).clearFile(file);
		}
	}

	@Override
	public void acquireReadLock() throws InterruptedException {
		checkThread();
		assert !fIsWriteLocked: "Read locks are not allowed while write-locked."; //$NON-NLS-1$
		super.acquireReadLock();
	}

	@Override
	public void releaseReadLock() {
		checkThread();
		assert !fIsWriteLocked: "Read locks are not allowed while write-locked."; //$NON-NLS-1$
		super.releaseReadLock();
		if (getReadLockCount() == 0)
			fThread= null;
	}

	@Override
	public void acquireWriteLock() throws InterruptedException {
		checkThread();
		assert !fIsWriteLocked: "Multiple write locks is not allowed"; //$NON-NLS-1$
		
		fWritableFragment.acquireWriteLock(getReadLockCount());
		fIsWriteLocked= true;
	}

	@Override
	public void releaseWriteLock() {
		releaseWriteLock(true);
	}

	@Override
	public void releaseWriteLock(boolean flush) {
		checkThread();
		assert fIsWriteLocked: "No write lock to be released"; //$NON-NLS-1$
		

		// Bug 297641: Result cache of read only providers needs to be cleared.
		int establishReadlockCount = getReadLockCount();
		if (establishReadlockCount == 0) {
			clearResultCache();
		}

		fIsWriteLocked= false;
		fWritableFragment.releaseWriteLock(establishReadlockCount, flush);
		
		if (establishReadlockCount == 0) {
			fThread= null;
		}
	}

	private void checkThread() {
		if (fThread == null) {
			fThread= Thread.currentThread();
		} else if (fThread != Thread.currentThread()) {
			throw new IllegalArgumentException("A writable index must not be used from multiple threads."); //$NON-NLS-1$
		}
	}
	
	@Override
	public void clearResultCache() {
		assert fIsWriteLocked: "Need to hold a write lock to clear result caches"; //$NON-NLS-1$
		super.clearResultCache();
	}

	@Override
	public void flush() throws CoreException {
		assert !fIsWriteLocked;
		fWritableFragment.flush();
	}

	@Override
	public long getDatabaseSizeBytes() {
		return fWritableFragment.getDatabaseSizeBytes();
	}

	@Override
	public void transferIncluders(IIndexFragmentFile source, IIndexFragmentFile target) throws CoreException {
		if (source == null || target == null || !isWritableFile(source) || !isWritableFile(target))
			throw new IllegalArgumentException();
		if (source.equals(target))
			return;
		target.transferIncluders(source);
	}
	
	@Override
	public void transferContext(IIndexFragmentFile source, IIndexFragmentFile target) throws CoreException {
		if (source == null || target == null || !isWritableFile(source) || !isWritableFile(target))
			throw new IllegalArgumentException();
		if (source.equals(target))
			return;
		target.transferContext(source);
	}
}
