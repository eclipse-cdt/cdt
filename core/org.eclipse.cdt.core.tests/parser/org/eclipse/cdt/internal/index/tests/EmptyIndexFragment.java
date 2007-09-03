/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.index.tests;

import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexLinkage;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IIndexFragmentInclude;
import org.eclipse.cdt.internal.core.index.IIndexFragmentName;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * An empty index fragment implementation
 * @since 4.0.1
 */
public class EmptyIndexFragment implements IIndexFragment {
	public void acquireReadLock() throws InterruptedException {}

	public IIndexFragmentBinding adaptBinding(IBinding binding)
			throws CoreException {
		return null;
	}

	public IIndexFragmentBinding adaptBinding(IIndexFragmentBinding proxy)
			throws CoreException {
		return null;
	}

	public IIndexFragmentBinding findBinding(IASTName astName) throws CoreException {
		return null;
	}

	public IIndexFragmentBinding[] findBindings(Pattern[] patterns,
			boolean isFullyQualified, IndexFilter filter,
			IProgressMonitor monitor) throws CoreException {
		return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	public IIndexFragmentBinding[] findBindings(char[][] names,
			IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	public IIndexFragmentBinding[] findBindingsForPrefix(char[] prefix,
			boolean filescope, IndexFilter filter, IProgressMonitor monitor)
			throws CoreException {
		return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	public IIndexFragmentInclude[] findIncludedBy(IIndexFragmentFile file)
			throws CoreException {
		return IIndexFragmentInclude.EMPTY_FRAGMENT_INCLUDES_ARRAY;
	}

	public IIndexFragmentName[] findNames(IIndexFragmentBinding binding,
			int flags) throws CoreException {
		return IIndexFragmentName.EMPTY_NAME_ARRAY;
	}

	public long getCacheHits() {
		return 0;
	}

	public long getCacheMisses() {
		return 0;
	}

	public IIndexFragmentFile getFile(IIndexFileLocation location)
			throws CoreException {
		return null;
	}

	public long getLastWriteAccess() {
		return 0;
	}

	public IIndexLinkage[] getLinkages() {
		return IIndexLinkage.EMPTY_INDEX_LINKAGE_ARRAY;
	}

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

	public void releaseReadLock() {}
	public void resetCacheCounters() {}
}
