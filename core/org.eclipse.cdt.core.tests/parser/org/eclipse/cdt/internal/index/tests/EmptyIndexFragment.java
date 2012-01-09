/*******************************************************************************
 * Copyright (c) 2007, 2011 Symbian Software Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *******************************************************************************/
package org.eclipse.cdt.internal.index.tests;

import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexLinkage;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.parser.ISignificantMacros;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFileSet;
import org.eclipse.cdt.internal.core.index.IIndexFragmentInclude;
import org.eclipse.cdt.internal.core.index.IIndexFragmentName;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * An empty index fragment implementation
 * @since 4.0.1
 */
public class EmptyIndexFragment implements IIndexFragment {
	@Override
	public void acquireReadLock() throws InterruptedException {}

	@Override
	public IIndexFragmentBinding adaptBinding(IBinding binding) {
		return null;
	}

	@Override
	public IIndexFragmentBinding findBinding(IASTName astName) {
		return null;
	}

	@Override
	public IIndexFragmentBinding[] findBindings(Pattern[] patterns,
			boolean isFullyQualified, IndexFilter filter,
			IProgressMonitor monitor) throws CoreException {
		return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	@Override
	public IIndexFragmentBinding[] findBindings(char[][] names,
			IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	@Override
	public IIndexFragmentBinding[] findBindings(char[] name,
			boolean filescope, IndexFilter filter, IProgressMonitor monitor)
			throws CoreException {
		return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	@Override
	public IIndexFragmentBinding[] findBindingsForPrefix(char[] prefix,
			boolean filescope, IndexFilter filter, IProgressMonitor monitor)
			throws CoreException {
		return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	@Override
	public IIndexFragmentBinding[] findBindingsForContentAssist(char[] prefix,
			boolean filescope, IndexFilter filter, IProgressMonitor monitor)
	throws CoreException {
		return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	@Override
	public IIndexMacro[] findMacros(char[] name, boolean isPrefix, boolean caseSensitive, IndexFilter filter, IProgressMonitor monitor) {
		return IIndexMacro.EMPTY_INDEX_MACRO_ARRAY;
	}

	@Override
	public IIndexFragmentInclude[] findIncludedBy(IIndexFragmentFile file)
			throws CoreException {
		return IIndexFragmentInclude.EMPTY_FRAGMENT_INCLUDES_ARRAY;
	}

	@Override
	public IIndexFragmentName[] findNames(IBinding binding,	int flags) {
		return IIndexFragmentName.EMPTY_NAME_ARRAY;
	}

	@Override
	public IIndexFragmentBinding[] findMacroContainers(Pattern pattern, IndexFilter filter, IProgressMonitor monitor) {
		return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	@Override
	public long getCacheHits() {
		return 0;
	}

	@Override
	public long getCacheMisses() {
		return 0;
	}

	@Override
	@Deprecated
	public IIndexFragmentFile getFile(int linkageID, IIndexFileLocation location)
			throws CoreException {
		return null;
	}

	@Override
	public IIndexFragmentFile getFile(int linkageID, IIndexFileLocation location,
			ISignificantMacros sigMacros) throws CoreException {
		return null;
	}

	@Override
	public IIndexFragmentFile[] getFiles(int linkageID, IIndexFileLocation location)
			throws CoreException {
		return IIndexFragmentFile.EMPTY_ARRAY;
	}
	
	@Override
	public IIndexFragmentFile[] getFiles(IIndexFileLocation location) throws CoreException {
		return IIndexFragmentFile.EMPTY_ARRAY;
	}

	@Override
	public long getLastWriteAccess() {
		return 0;
	}

	@Override
	public IIndexLinkage[] getLinkages() {
		return IIndexLinkage.EMPTY_INDEX_LINKAGE_ARRAY;
	}

	@Override
	public String getProperty(String key) throws CoreException {
		if(IIndexFragment.PROPERTY_FRAGMENT_ID.equals(key)) {
			return "org.eclipse.cdt.internal.core.index.EmptyIndexFragment"; //$NON-NLS-1$
		}
		if(IIndexFragment.PROPERTY_FRAGMENT_FORMAT_ID.equals(key)) {
			return "org.eclipse.cdt.internal.core.index.EmptyIndexFragmentFormat"; //$NON-NLS-1$
		}
		if(IIndexFragment.PROPERTY_FRAGMENT_FORMAT_VERSION.equals(key)) {
			return "0"; //$NON-NLS-1$
		}
		return null;
	}

	@Override
	public void releaseReadLock() {}

	@Override
	public boolean hasWaitingReaders() {
		return false;
	}

	@Override
	public void resetCacheCounters() {}

	@Override
	public IIndexFragmentFileSet createFileSet() {
		return null;
	}
	@Override
	public IIndexFragmentFile[] getAllFiles() {
		return IIndexFragmentFile.EMPTY_ARRAY;
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
	}

	@Override
	public IIndexScope[] getInlineNamespaces() {
		return IIndexScope.EMPTY_INDEX_SCOPE_ARRAY;
	}
}
