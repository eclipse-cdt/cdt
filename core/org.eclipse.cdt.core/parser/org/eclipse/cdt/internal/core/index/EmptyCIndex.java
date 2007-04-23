/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Bryan Wilkinson (QNX)
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.index;

import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

final public class EmptyCIndex implements IIndex {
	public static IIndex INSTANCE= new EmptyCIndex();
	
	private EmptyCIndex() {
	}
	
	public IIndexName[] findDeclarations(IBinding binding) {
		return IIndexFragmentName.EMPTY_NAME_ARRAY;
	}

	public IIndexName[] findDefinitions(IBinding binding) {
		return IIndexFragmentName.EMPTY_NAME_ARRAY;
	}

	public IIndexName[] findReferences(IBinding binding) {
		return IIndexFragmentName.EMPTY_NAME_ARRAY;
	}

	public IIndexName[] findNames(IBinding binding, int flags) {
		return IIndexFragmentName.EMPTY_NAME_ARRAY;
	}

	public IIndexFile getFile(IIndexFileLocation location) {
		return null;
	}

	public IIndexFile resolveInclude(IIndexInclude include) {
		return null;
	}

	public IIndexInclude[] findIncludedBy(IIndexFile file) {
		return IIndexInclude.EMPTY_INCLUDES_ARRAY;
	}

	public IIndexInclude[] findIncludedBy(IIndexFile file, int depth) {
		return IIndexInclude.EMPTY_INCLUDES_ARRAY;
	}

	public IIndexInclude[] findIncludes(IIndexFile file) {
		return IIndexInclude.EMPTY_INCLUDES_ARRAY;
	}

	public IIndexInclude[] findIncludes(IIndexFile file, int depth) {
		return IIndexInclude.EMPTY_INCLUDES_ARRAY;
	}

	public void acquireReadLock() {
	}

	public void releaseReadLock() {
	}

	public long getLastWriteAccess() {
		return 0;
	}

	public IIndexBinding findBinding(IName name) {
		return null;
	}

	public IIndexBinding[] findBindings(Pattern pattern, boolean isFullyQualified, IndexFilter filter, IProgressMonitor monitor) {
		return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	public IIndexBinding[] findBindings(Pattern[] pattern, boolean isFullyQualified, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	public IIndexBinding adaptBinding(IBinding binding) {
		return null;
	}

	public IIndexBinding[] findBindingsForPrefix(char[] prefix, boolean filescope, IndexFilter filter, IProgressMonitor monitor) {
		return IIndexBinding.EMPTY_INDEX_BINDING_ARRAY;
	}
	
	public IIndexBinding[] findBindings(char[][] names, IndexFilter filter,
			IProgressMonitor monitor) {
		return IIndexBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	public IIndexBinding[] findBindings(char[] names, IndexFilter filter,
			IProgressMonitor monitor)  {
		return IIndexBinding.EMPTY_INDEX_BINDING_ARRAY;
	}
}