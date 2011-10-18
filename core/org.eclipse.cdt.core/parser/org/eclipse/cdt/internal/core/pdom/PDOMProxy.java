/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexLinkage;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.parser.ISignificantMacros;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFileSet;
import org.eclipse.cdt.internal.core.index.IIndexFragmentInclude;
import org.eclipse.cdt.internal.core.index.IIndexFragmentName;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.pdom.PDOM.ChangeEvent;
import org.eclipse.cdt.internal.core.pdom.PDOM.DebugLockInfo;
import org.eclipse.cdt.internal.core.pdom.PDOM.IListener;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The PDOMProxy is returned by the PDOMManager before the indexer kicks in. Also and more
 * importantly it is returned when the indexer has been shut down (clients may not be aware
 * of this yet). Doing that prevents the creation of empty pdoms for deleted projects.
 */
public class PDOMProxy implements IPDOM {
	private PDOM fDelegate;
	private int fReadLockCount;
	private Set<IListener> fListeners= new HashSet<IListener>();
	private Map<Thread, DebugLockInfo> fLockDebugging;

	public PDOMProxy() {
		if (PDOM.sDEBUG_LOCKS) {
			fLockDebugging= new HashMap<Thread, DebugLockInfo>();
		}
	}

	@Override
	public synchronized void acquireReadLock() throws InterruptedException {
		if (fDelegate != null) {
			fDelegate.acquireReadLock();
		} else {
			fReadLockCount++;
			if (PDOM.sDEBUG_LOCKS) {
				PDOM.incReadLock(fLockDebugging);
			}
		}
	}

	@Override
	public IIndexMacro[] findMacros(char[] name, boolean isPrefix, boolean caseSensitive,
			IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		if (fDelegate != null)
			return fDelegate.findMacros(name, isPrefix, caseSensitive, filter, monitor);
		return IIndexMacro.EMPTY_INDEX_MACRO_ARRAY;
	}

	@Override
	public synchronized IIndexFragmentBinding adaptBinding(IBinding binding) throws CoreException {
		if (fDelegate != null)
			return fDelegate.adaptBinding(binding);
		return null;
	}

	@Override
	public synchronized IIndexFragmentBinding findBinding(IASTName astName) throws CoreException {
		if (fDelegate != null)
			return fDelegate.findBinding(astName);
		return null;
	}

	@Override
	public synchronized IIndexFragmentBinding[] findBindings(char[][] names, IndexFilter filter,
			IProgressMonitor monitor) throws CoreException {
		if (fDelegate != null)
			return fDelegate.findBindings(names, filter, monitor);

		return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	@Override
	public synchronized IIndexFragmentBinding[] findBindings(Pattern[] patterns, boolean isFullyQualified,
			IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		if (fDelegate != null)
			return fDelegate.findBindings(patterns, isFullyQualified, filter, monitor);

		return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	@Override
	public synchronized IIndexFragmentBinding[] findBindings(char[] name, boolean filescope,
			IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		if (fDelegate != null)
			return fDelegate.findBindings(name, filescope, filter, monitor);

		return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	@Override
	public synchronized IIndexFragmentBinding[] findBindingsForPrefix(char[] prefix, boolean filescope,
			IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		if (fDelegate != null)
			return fDelegate.findBindingsForPrefix(prefix, filescope, filter, monitor);

		return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	@Override
	public synchronized IIndexFragmentBinding[] findBindingsForContentAssist(char[] prefix, boolean filescope,
			IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		if (fDelegate != null)
			return fDelegate.findBindingsForContentAssist(prefix, filescope, filter, monitor);

		return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	@Override
	public synchronized IIndexFragmentInclude[] findIncludedBy(IIndexFragmentFile file) throws CoreException {
		if (fDelegate != null)
			return fDelegate.findIncludedBy(file);

		return new IIndexFragmentInclude[0];
	}

	@Override
	public synchronized IIndexFragmentName[] findNames(IBinding binding, int flags)
			throws CoreException {
		if (fDelegate != null)
			return fDelegate.findNames(binding, flags);

		return IIndexFragmentName.EMPTY_NAME_ARRAY;
	}

	@Override
	public synchronized long getCacheHits() {
		if (fDelegate != null)
			return fDelegate.getCacheHits();

		return 0;
	}

	@Override
	public synchronized long getCacheMisses() {
		if (fDelegate != null)
			return fDelegate.getCacheMisses();

		return 0;
	}

	@Deprecated
	@Override
	public synchronized IIndexFragmentFile getFile(int linkageID, IIndexFileLocation location) throws CoreException {
		if (fDelegate != null)
			return fDelegate.getFile(linkageID, location);

		return null;
	}

	@Override
	public IIndexFragmentFile getFile(int linkageID, IIndexFileLocation location,
			ISignificantMacros sigMacros) throws CoreException {
		if (fDelegate != null)
			return fDelegate.getFile(linkageID, location, sigMacros);

		return null;
	}

	@Override
	public IIndexFragmentFile[] getFiles(int linkageID, IIndexFileLocation location)
			throws CoreException {
		if (fDelegate != null)
			return fDelegate.getFiles(linkageID, location);

		return IIndexFragmentFile.EMPTY_ARRAY;
	}

	@Override
	public synchronized IIndexFragmentFile[] getFiles(IIndexFileLocation location) throws CoreException {
		if (fDelegate != null)
			return fDelegate.getFiles(location);

		return IIndexFragmentFile.EMPTY_ARRAY;
	}

	@Override
	public synchronized long getLastWriteAccess() {
		if (fDelegate != null)
			return fDelegate.getLastWriteAccess();

		return 0;
	}

	@Override
	public synchronized IIndexLinkage[] getLinkages() {
		if (fDelegate != null)
			return fDelegate.getLinkages();

		return new IIndexLinkage[0];
	}

	@Override
	public synchronized String getProperty(String propertyName) throws CoreException {
		if (fDelegate != null)
			return fDelegate.getProperty(propertyName);

		return null;
	}

	@Override
	public synchronized void releaseReadLock() {
		// read-locks not forwarded to delegate need to be released here
		if (fReadLockCount > 0) {
			fReadLockCount--;
			if (PDOM.sDEBUG_LOCKS)
				PDOM.decReadLock(fLockDebugging);
		} else if (fDelegate != null) {
			fDelegate.releaseReadLock();
		}
	}

	@Override
	public boolean hasWaitingReaders() {
		return fDelegate != null && fDelegate.hasWaitingReaders();
	}

	@Override
	public synchronized void resetCacheCounters() {
		if (fDelegate != null)
			fDelegate.resetCacheCounters();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public synchronized Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(PDOMProxy.class)) {
			return this;
		}
		return null;
	}

	@Override
	public synchronized void addListener(IListener listener) {
		if (fDelegate != null) {
			fDelegate.addListener(listener);
		} else {
			fListeners.add(listener);
		}
	}

	@Override
	public synchronized PDOMLinkage[] getLinkageImpls() {
		if (fDelegate != null)
			return fDelegate.getLinkageImpls();

		return new PDOMLinkage[0];
	}

	@Override
	public synchronized void removeListener(IListener listener) {
		if (fDelegate != null) {
			fDelegate.removeListener(listener);
		} else {
			fListeners.remove(listener);
		}
	}

	public synchronized void setDelegate(WritablePDOM pdom) {
		fDelegate= pdom;
		try {
			while (fReadLockCount > 0) {
				pdom.acquireReadLock();
				fReadLockCount--;
			}
			if (PDOM.sDEBUG_LOCKS) {
				pdom.adjustThreadForReadLock(fLockDebugging);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		for (IListener listener : fListeners) {
			pdom.addListener(listener);
		}
		ChangeEvent event= new ChangeEvent();
		event.setReloaded();
		for (IListener listener : fListeners) {
			listener.handleChange(fDelegate, event);
		}
	}

	@Override
	public IIndexFragmentFileSet createFileSet() {
		return new PDOMFileSet();
	}

	@Override
	public synchronized IIndexFragmentFile[] getAllFiles() throws CoreException {
		if (fDelegate != null)
			return fDelegate.getAllFiles();
		return IIndexFragmentFile.EMPTY_ARRAY;
	}

	@Override
	public synchronized IIndexFragmentBinding[] findMacroContainers(Pattern pattern, IndexFilter filter,
			IProgressMonitor monitor) throws CoreException {
		if (fDelegate != null) {
			return fDelegate.findMacroContainers(pattern, filter, monitor);
		}
		return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	@Override
	public Object getCachedResult(Object key) {
		return null;
	}

	@Override
	public Object putCachedResult(Object key, Object value, boolean replace) {
		return value;
	}

	@Override
	public void clearResultCache() {
		if (fDelegate != null)
			fDelegate.clearResultCache();
	}

	@Override
	public IIndexScope[] getInlineNamespaces() throws CoreException {
		if (fDelegate != null)
			return fDelegate.getInlineNamespaces();

		return IIndexScope.EMPTY_INDEX_SCOPE_ARRAY;
	}
}
