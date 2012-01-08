/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Bryan Wilkinson (QNX)
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.index;

import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.parser.ISignificantMacros;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

final public class EmptyCIndex implements IIndex {
	public static IIndex INSTANCE= new EmptyCIndex();

	private EmptyCIndex() {
	}

	@Override
	public IIndexName[] findDeclarations(IBinding binding) {
		return IIndexFragmentName.EMPTY_NAME_ARRAY;
	}

	@Override
	public IIndexName[] findDefinitions(IBinding binding) {
		return IIndexFragmentName.EMPTY_NAME_ARRAY;
	}

	@Override
	public IIndexName[] findReferences(IBinding binding) {
		return IIndexFragmentName.EMPTY_NAME_ARRAY;
	}

	@Override
	public IIndexName[] findNames(IBinding binding, int flags) {
		return IIndexFragmentName.EMPTY_NAME_ARRAY;
	}

	@Override
	@Deprecated
	public IIndexFile getFile(int linkageID, IIndexFileLocation location) {
		return null;
	}

	@Override
	public IIndexFile getFile(int linkageID, IIndexFileLocation location,
			ISignificantMacros significantFiles) throws CoreException {
		return null;
	}

	@Override
	public IIndexFile[] getFiles(int linkageID, IIndexFileLocation location) throws CoreException {
		return IIndexFile.EMPTY_FILE_ARRAY;
	}

	@Override
	public IIndexFile[] getFiles(IIndexFileLocation location) {
		return IIndexFile.EMPTY_FILE_ARRAY;
	}

	@Override
	public IIndexFile resolveInclude(IIndexInclude include) {
		return null;
	}

	@Override
	public IIndexInclude[] findIncludedBy(IIndexFile file) {
		return IIndexInclude.EMPTY_INCLUDES_ARRAY;
	}

	@Override
	public IIndexInclude[] findIncludedBy(IIndexFile file, int depth) {
		return IIndexInclude.EMPTY_INCLUDES_ARRAY;
	}

	@Override
	public IIndexInclude[] findIncludes(IIndexFile file) {
		return IIndexInclude.EMPTY_INCLUDES_ARRAY;
	}

	@Override
	public IIndexInclude[] findIncludes(IIndexFile file, int depth) {
		return IIndexInclude.EMPTY_INCLUDES_ARRAY;
	}

	@Override
	public void acquireReadLock() {
	}

	@Override
	public void releaseReadLock() {
	}

	@Override
	public boolean hasWaitingReaders() {
		return false;
	}

	@Override
	public long getLastWriteAccess() {
		return 0;
	}

	@Override
	public IIndexBinding findBinding(IName name) {
		return null;
	}

	@Override
	public IIndexBinding[] findBindings(Pattern pattern, boolean isFullyQualified,
			IndexFilter filter, IProgressMonitor monitor) {
		return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	@Override
	public IIndexBinding[] findBindings(Pattern[] pattern, boolean isFullyQualified,
			IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	@Override
	public IIndexBinding adaptBinding(IBinding binding) {
		return null;
	}

	@Override
	public IIndexBinding[] findBindingsForPrefix(char[] prefix, boolean filescope,
			IndexFilter filter, IProgressMonitor monitor) {
		return IIndexBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	@Override
	public IIndexBinding[] findBindingsForContentAssist(char[] prefix, boolean filescope,
			IndexFilter filter, IProgressMonitor monitor) {
		return IIndexBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	@Override
	public IIndexBinding[] findBindings(char[][] names, IndexFilter filter,
			IProgressMonitor monitor) {
		return IIndexBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	@Override
	public IIndexBinding[] findBindings(char[] names, IndexFilter filter,
			IProgressMonitor monitor)  {
		return IIndexBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	@Override
	public IIndexMacro[] findMacros(char[] name, IndexFilter filter, IProgressMonitor monitor) {
		return IIndexMacro.EMPTY_INDEX_MACRO_ARRAY;
	}

	@Override
	public IIndexMacro[] findMacrosForPrefix(char[] prefix, IndexFilter filter, IProgressMonitor monitor) {
		return IIndexMacro.EMPTY_INDEX_MACRO_ARRAY;
	}

	@Override
	public IIndexFileSet createFileSet() {
		return new IndexFileSet();
	}

	@Override
	public IIndexFile[] getAllFiles() {
		return IIndexFile.EMPTY_FILE_ARRAY;
	}

	@Override
	public IIndexBinding[] findBindings(char[] name, boolean fileScopeOnly,	IndexFilter filter, IProgressMonitor monitor) {
		return IIndexBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	@Override
	public IIndexBinding[] findMacroContainers(Pattern pattern, IndexFilter filter, IProgressMonitor monitor) {
		return IIndexBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	@Override
	public IScope[] getInlineNamespaces() {
		return new IScope[0];
	}
}