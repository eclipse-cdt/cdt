/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Bryan Wilkinson (QNX)
 *     Andrew Ferguson (Symbian)
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
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
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.index.composite.CompositingNotImplementedError;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;
import org.eclipse.cdt.internal.core.index.composite.c.CCompositesFactory;
import org.eclipse.cdt.internal.core.index.composite.cpp.CPPCompositesFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

public class CIndex implements IIndex {
	/**
	 * If this constant is set, for logical index objects with only
	 * one fragment, composite binding wrappers will not be used.
	 */
	private static final boolean SPECIALCASE_SINGLES = true;

	private final IIndexFragment[] fFragments;
	private int fReadLock;
	private ICompositesFactory cppCF, cCF, fCF;

	/**
	 * Creates an index consisting of one or more fragments.
	 * 
	 * @param fragments Fragments constituting the index. If there are extended fragments,
	 * they are located in the array after the PDOM fragments for the same project. 
	 */
	public CIndex(IIndexFragment[] fragments) {
		fFragments= fragments;
	}

	@Override
	public IIndexBinding findBinding(IName name) throws CoreException {
		if (name instanceof IIndexFragmentName) {
			return adaptBinding(((IIndexFragmentName) name).getBinding());
		} else if (name instanceof IASTName) {
			if (SPECIALCASE_SINGLES && fFragments.length == 1) {
				return fFragments[0].findBinding((IASTName) name);
			} else {
				for (IIndexFragment fragment : fFragments) {
					IIndexFragmentBinding binding= fragment.findBinding((IASTName) name);
					if (binding != null) {
						return getCompositesFactory(binding.getLinkage().getLinkageID()).getCompositeBinding(binding);
					}
				} 
			}
		}
		return null;
	}

	@Override
	public IIndexBinding[] findBindings(Pattern pattern, boolean isFullyQualified, IndexFilter filter,
			IProgressMonitor monitor) throws CoreException {
		return findBindings(new Pattern[] { pattern }, isFullyQualified, filter, monitor);
	}

	@Override
	public IIndexBinding[] findBindings(Pattern[] patterns, boolean isFullyQualified, IndexFilter filter,
			IProgressMonitor monitor) throws CoreException {
		if (SPECIALCASE_SINGLES && fFragments.length == 1) {
			return fFragments[0].findBindings(patterns, isFullyQualified, filter, monitor);
		} else {
			List<IIndexBinding[]> result = new ArrayList<IIndexBinding[]>();
			ILinkage[] linkages = Linkage.getIndexerLinkages();
			for (ILinkage linkage : linkages) {
				if (filter.acceptLinkage(linkage)) {
					IIndexFragmentBinding[][] fragmentBindings = new IIndexFragmentBinding[fFragments.length][];
					for (int i = 0; i < fFragments.length; i++) {
						try {
							IBinding[] part = fFragments[i].findBindings(patterns, isFullyQualified,
									retargetFilter(linkage, filter), monitor);
							fragmentBindings[i] = new IIndexFragmentBinding[part.length];
							System.arraycopy(part, 0, fragmentBindings[i], 0, part.length);
						} catch (CoreException e) {
							CCorePlugin.log(e);
							fragmentBindings[i] = IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
						}
					}
					ICompositesFactory factory = getCompositesFactory(linkage.getLinkageID());
					result.add(factory.getCompositeBindings(fragmentBindings));
				}
			}
			return flatten(result);
		}
	}

	@Override
	public IIndexBinding[] findMacroContainers(Pattern pattern, IndexFilter filter, IProgressMonitor monitor)
			throws CoreException {
		if (SPECIALCASE_SINGLES && fFragments.length == 1) {
			 return fFragments[0].findMacroContainers(pattern, filter, monitor);
		} else {
			List<IIndexBinding[]> result = new ArrayList<IIndexBinding[]>();
			ILinkage[] linkages = Linkage.getIndexerLinkages();
			for (ILinkage linkage : linkages) {
				if (filter.acceptLinkage(linkage)) {
					IIndexFragmentBinding[][] fragmentBindings = new IIndexFragmentBinding[fFragments.length][];
					for (int i = 0; i < fFragments.length; i++) {
						try {
							IBinding[] part = fFragments[i].findMacroContainers(pattern,
									retargetFilter(linkage, filter), monitor);
							fragmentBindings[i] = new IIndexFragmentBinding[part.length];
							System.arraycopy(part, 0, fragmentBindings[i], 0, part.length);
						} catch (CoreException e) {
							CCorePlugin.log(e);
							fragmentBindings[i] = IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
						}
					}
					ICompositesFactory factory = getCompositesFactory(linkage.getLinkageID());
					result.add(factory.getCompositeBindings(fragmentBindings));
				}
			}
			return flatten(result);
		}
	}

	@Override
	public IIndexName[] findNames(IBinding binding, int flags) throws CoreException {
		ArrayList<IIndexFragmentName> result= new ArrayList<IIndexFragmentName>();
		if (binding instanceof ICPPUsingDeclaration) {
			IBinding[] bindings= ((ICPPUsingDeclaration) binding).getDelegates();
			if (bindings == null || bindings.length == 0) {
				return new IIndexName[0];
			}
			if (bindings.length > 1) {
				ArrayList<IIndexName> multi= new ArrayList<IIndexName>();
				for (IBinding b : bindings) {
					multi.addAll(Arrays.asList(findNames(b, flags)));
				}
				return multi.toArray(new IIndexName[multi.size()]);
			}
			binding= bindings[0];
		}

		// Collect the names from all fragments. Since the same file may be represented by multiple
		// variants in one or more index fragments, we need to filter out duplicate names.
		// See bug 192352. 
		// Read only fragments can be superseded by what the indexer writes into
		// a writable fragment. Therefore names from a read-only fragment are
		// ignored if there is a match in a writable fragment.
		HashSet<NameKey> encounteredNames = new HashSet<NameKey>();
		for (IIndexFragment fragment : fFragments) {
			if (fragment instanceof IWritableIndexFragment) {
				final IIndexFragmentName[] names = fragment.findNames(binding, flags);
				for (IIndexFragmentName name : names) {
					if (encounteredNames.add(new NameKey(name, true))) {
						result.add(name);
					}
				}
			}
		}
		for (IIndexFragment fragment : fFragments) {
			if (!(fragment instanceof IWritableIndexFragment)) {
				final IIndexFragmentName[] names = fragment.findNames(binding, flags);
				for (IIndexFragmentName name : names) {
					if (encounteredNames.add(new NameKey(name, false))) {
						result.add(name);
					}
				}
			}
		}
		return result.toArray(new IIndexName[result.size()]);
	}

	@Override
	public IIndexName[] findDeclarations(IBinding binding) throws CoreException {
		return findNames(binding, FIND_DECLARATIONS_DEFINITIONS);
	}

	@Override
	public IIndexName[] findDefinitions(IBinding binding) throws CoreException {
		return findNames(binding, FIND_DEFINITIONS);
	}

	@Override
	public IIndexName[] findReferences(IBinding binding) throws CoreException {
		return findNames(binding, FIND_REFERENCES);
	}

	@Override
	@Deprecated
	public IIndexFile getFile(int linkageID, IIndexFileLocation location) throws CoreException {
		for (IIndexFragment fragment : fFragments) {
			IIndexFragmentFile candidate= fragment.getFile(linkageID, location);
			if (candidate != null && candidate.hasContent()) {
				return candidate;
			}
		}
		return null;
	}

	@Override
	public IIndexFile getFile(int linkageID, IIndexFileLocation location,
			ISignificantMacros significantMacros) throws CoreException {
		for (IIndexFragment fragment : fFragments) {
			IIndexFragmentFile candidate= fragment.getFile(linkageID, location, significantMacros);
			if (candidate != null && candidate.hasContent()) {
				return candidate;
			}
		}
		return null;
	}

	@Override
	public IIndexFile[] getFiles(int linkageID, IIndexFileLocation location) throws CoreException {
		if (location == null) {
			return IIndexFile.EMPTY_FILE_ARRAY;
		}
		Set<ISignificantMacros> handled = new HashSet<ISignificantMacros>();
		ArrayList<IIndexFragmentFile> result= new ArrayList<IIndexFragmentFile>();
		for (IIndexFragment fragment : fFragments) {
			IIndexFragmentFile[] candidates= fragment.getFiles(linkageID, location);
			for (IIndexFragmentFile candidate : candidates) {
				if (candidate.hasContent()) {
					ISignificantMacros macroKey = candidate.getSignificantMacros();
					if (handled.add(macroKey)) {
						result.add(candidate);
					}
				}
			}
		}
		if (result.isEmpty()) {
			return IIndexFile.EMPTY_FILE_ARRAY;
		}
		return result.toArray(new IIndexFile[result.size()]);
	}

	@Override
	public IIndexFile[] getFiles(IIndexFileLocation location) throws CoreException {
		if (location == null) {
			return IIndexFile.EMPTY_FILE_ARRAY;
		}
		Set<FileContentKey> keys = new HashSet<FileContentKey>();
		ArrayList<IIndexFragmentFile> result= new ArrayList<IIndexFragmentFile>();
		for (IIndexFragment fragment : fFragments) {
			IIndexFragmentFile[] candidates= fragment.getFiles(location);
			for (IIndexFragmentFile candidate : candidates) {
				if (candidate.hasContent()) {
					if (keys.add(new FileContentKey(candidate.getLinkageID(), candidate.getLocation(), candidate.getSignificantMacros()))) {
						result.add(candidate);
					}
				}
			}
		}
		if (result.isEmpty()) {
			return IIndexFile.EMPTY_FILE_ARRAY;
		}
		return result.toArray(new IIndexFile[result.size()]);
	}

	@Override
	public IIndexFile resolveInclude(IIndexInclude include) throws CoreException {
		IIndexFragmentInclude fragmentInclude = (IIndexFragmentInclude) include;
		IIndexFragmentFile result= fragmentInclude.getIncludes();
		if (result == null)
			return null;
		
		if (result.hasContent()) {
			return result;
		}

		return getFile(result.getLinkageID(), result.getLocation(), result.getSignificantMacros());
	}

	@Override
	public IIndexInclude[] findIncludedBy(IIndexFile file) throws CoreException {
		return findIncludedBy(file, 0);
	}

	@Override
	public IIndexInclude[] findIncludedBy(IIndexFile file, int depth) throws CoreException {
		List<IIndexInclude> result= new ArrayList<IIndexInclude>();
		findIncludedBy(file.getLinkageID(), Collections.singletonList(file), result, depth,
				new HashSet<FileContentKey>());
		return result.toArray(new IIndexInclude[result.size()]);
	}

	public void findIncludedBy(int linkageID, List<IIndexFile> in, List<IIndexInclude> out, int depth,
			HashSet<FileContentKey> handled) throws CoreException {
		List<IIndexFile> nextLevel= depth != 0 ? new LinkedList<IIndexFile>() : null;
		for (IIndexFile iIndexFile : in) {
			IIndexFragmentFile file = (IIndexFragmentFile) iIndexFile;
			for (IIndexFragment fragment : fFragments) {
				IIndexInclude[] includedBy= fragment.findIncludedBy(file);
				for (IIndexInclude include : includedBy) {
					final IIndexFile includer = include.getIncludedBy();
					FileContentKey key= new FileContentKey(linkageID, includer.getLocation(), includer.getSignificantMacros());
					if (handled.add(key)) {
						out.add(include);
						if (nextLevel != null) {
							nextLevel.add(includer);
						}
					}
				}
			}
		}
		if (nextLevel == null || nextLevel.isEmpty()) {
			return;
		}
		if (depth > 0) {
			depth--;
		}
		findIncludedBy(linkageID, nextLevel, out, depth, handled);
	}

	@Override
	public IIndexInclude[] findIncludes(IIndexFile file) throws CoreException {
		return findIncludes(file, 0);
	}

	@Override
	public IIndexInclude[] findIncludes(IIndexFile file, int depth) throws CoreException {
		List<IIndexInclude> result= new ArrayList<IIndexInclude>();
		findIncludes(Collections.singletonList(file), result, depth, new HashSet<Object>());
		return result.toArray(new IIndexInclude[result.size()]);
	}

	private void findIncludes(List<IIndexFile> in, List<IIndexInclude> out, int depth,
			HashSet<Object> handled) throws CoreException {
		List<IIndexFile> nextLevel= depth != 0 ? new LinkedList<IIndexFile>() : null;
		for (IIndexFile iIndexFile : in) {
			IIndexFragmentFile file = (IIndexFragmentFile) iIndexFile;
			IIndexInclude[] includes= file.getIncludes();
			for (IIndexInclude include : includes) {
				IIndexFileLocation target= include.getIncludesLocation();
				Object key= target != null ? (Object) target : include.getFullName();
				if (handled.add(key)) {
					out.add(include);
					if (nextLevel != null) {
						IIndexFile includedByFile= resolveInclude(include);
						if (includedByFile != null) {
							nextLevel.add(includedByFile);
						}
					}
				}
			}
		}
		if (nextLevel == null || nextLevel.isEmpty()) {
			return;
		}
		if (depth > 0) {
			depth--;
		}
		findIncludes(nextLevel, out, depth, handled);
	}

	@Override
	public synchronized void acquireReadLock() throws InterruptedException {
		if (++fReadLock == 1) {
			int i= 0;
			try {
				for (i = 0; i < fFragments.length; i++) {
					fFragments[i].acquireReadLock();
				}
			} finally {
				if (i < fFragments.length) {
					// rollback
					fReadLock--;
					while (--i >= 0) {
						fFragments[i].releaseReadLock();
					}
				}
			}
		}
	}

	@Override
	public synchronized void releaseReadLock() {
		if (--fReadLock == 0) {
			for (IIndexFragment fragment : fFragments) {
				fragment.releaseReadLock();
			}
		}
	}

	protected synchronized int getReadLockCount() {
		return fReadLock;
	}

	@Override
	public boolean hasWaitingReaders() {
		for (IIndexFragment fragment : fFragments) {
			if (fragment.hasWaitingReaders()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public long getLastWriteAccess() {
		long result= 0;
		for (IIndexFragment fragment : fFragments) {
			result= Math.max(result, fragment.getLastWriteAccess());
		}
		return result;
	}

	@Override
	public IIndexBinding[] findBindings(char[][] names, IndexFilter filter, IProgressMonitor monitor)
			throws CoreException {
		if (SPECIALCASE_SINGLES && fFragments.length == 1) {
			try {
				return fFragments[0].findBindings(names, filter, monitor);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
			}
		} else {
			if (monitor == null) {
				monitor= new NullProgressMonitor();
			}
			List<IIndexBinding[]> result = new ArrayList<IIndexBinding[]>();
			ILinkage[] linkages = Linkage.getIndexerLinkages();
			monitor.beginTask(Messages.CIndex_FindBindingsTask_label, fFragments.length * linkages.length);
			for (ILinkage linkage : linkages) {
				if (filter.acceptLinkage(linkage)) {
					IIndexFragmentBinding[][] fragmentBindings = new IIndexFragmentBinding[fFragments.length][];
					for (int i = 0; i < fFragments.length; i++) {
						try {
							IBinding[] part = fFragments[i].findBindings(names,
									retargetFilter(linkage, filter), new SubProgressMonitor(monitor, 1));
							fragmentBindings[i] = new IIndexFragmentBinding[part.length];
							System.arraycopy(part, 0, fragmentBindings[i], 0, part.length);
						} catch (CoreException e) {
							CCorePlugin.log(e);
							fragmentBindings[i] = IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
						}
					}
					ICompositesFactory factory = getCompositesFactory(linkage.getLinkageID());
					result.add(factory.getCompositeBindings(fragmentBindings));
				}
			}
			monitor.done();
			return flatten(result);
		}
	}

	@Override
	public IIndexBinding adaptBinding(IBinding binding) {
		try {
			if (SPECIALCASE_SINGLES && fFragments.length == 1) {
				return fFragments[0].adaptBinding(binding);
			} else {
				for (IIndexFragment fragment : fFragments) {
					IIndexFragmentBinding adaptedBinding= fragment.adaptBinding(binding);
					if (adaptedBinding != null) {
						return getCompositesFactory(binding.getLinkage().getLinkageID()).getCompositeBinding(adaptedBinding);
					}
				}
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	@Override
	public IIndexBinding[] findBindings(char[] name, IndexFilter filter, IProgressMonitor monitor)
			throws CoreException {
		return findBindings(name, true, filter, monitor);
	}

	/*
	 * Non-API
	 */

	private IIndexBinding[] flatten(List<IIndexBinding[]> bindingArrays) {
		int size = 0;
		for (int i = 0; i<bindingArrays.size(); i++) {
			size += bindingArrays.get(i).length;
		}
		IIndexBinding[] result = new IIndexBinding[size];
		int offset = 0;
		for (int i = 0; i<bindingArrays.size(); i++) {
			IBinding[] src = bindingArrays.get(i);
			System.arraycopy(src, 0, result, offset, src.length);
			offset += src.length;
		}
		return result;
	}

	public IIndexFragment[] getFragments() {
		return fFragments;
	}

	public IIndexFragmentBinding[] findEquivalentBindings(IBinding binding) throws CoreException {
		List<IIndexFragmentBinding> result = new ArrayList<IIndexFragmentBinding>();
		for (IIndexFragment fragment : fFragments) {
			IIndexFragmentBinding adapted = fragment.adaptBinding(binding);
			if (adapted != null) {
				result.add(adapted);
			}
		}
		return result.toArray(new IIndexFragmentBinding[result.size()]);
	}

	private ICompositesFactory getCompositesFactory(int linkageID) {
		switch (linkageID) {
		case ILinkage.CPP_LINKAGE_ID:
			if (cppCF == null) {
				cppCF = new CPPCompositesFactory(this);
			}
			return cppCF;

		case ILinkage.C_LINKAGE_ID:
			if (cCF == null) {
				cCF = new CCompositesFactory(this);
			}
			return cCF;

		case ILinkage.FORTRAN_LINKAGE_ID:
			if (fCF == null) {
				fCF = new CCompositesFactory(this);
			}
			// This is a placeholder - it will throw CompositingNotImplementedError
			// if non-empty (non-c) results are returned by a fragment
			return fCF;
		}
		throw new CompositingNotImplementedError();
	}

	private IndexFilter retargetFilter(final ILinkage linkage, final IndexFilter filter) {
		return new IndexFilter() {
			@Override
			public boolean acceptBinding(IBinding binding) throws CoreException {
				return filter.acceptBinding(binding);
			}
			@Override
			public boolean acceptLinkage(ILinkage other) {
				return linkage.getLinkageID() == other.getLinkageID();
			}
		};
	}

	@Override
	public IIndexBinding[] findBindingsForPrefix(char[] prefix, boolean filescope, IndexFilter filter,
			IProgressMonitor monitor) throws CoreException {
		if (SPECIALCASE_SINGLES && fFragments.length == 1) {
			return fFragments[0].findBindingsForPrefix(prefix, filescope, filter, monitor);
		} else {
			List<IIndexBinding[]> result = new ArrayList<IIndexBinding[]>();
			ILinkage[] linkages = Linkage.getIndexerLinkages();
			for (ILinkage linkage : linkages) {
				if (filter.acceptLinkage(linkage)) {
					IIndexFragmentBinding[][] fragmentBindings = new IIndexFragmentBinding[fFragments.length][];
					for (int i = 0; i < fFragments.length; i++) {
						try {
							IBinding[] part = fFragments[i].findBindingsForPrefix(prefix, filescope,
									retargetFilter(linkage, filter), monitor);
							fragmentBindings[i] = new IIndexFragmentBinding[part.length];
							System.arraycopy(part, 0, fragmentBindings[i], 0, part.length);
						} catch (CoreException e) {
							CCorePlugin.log(e);
							fragmentBindings[i] = IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
						}
					}
					ICompositesFactory factory = getCompositesFactory(linkage.getLinkageID());
					result.add(factory.getCompositeBindings(fragmentBindings));
				}
			}
			return flatten(result);
		}
	}

	@Override
	public IIndexBinding[] findBindingsForContentAssist(char[] prefix, boolean filescope,
			IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		if (SPECIALCASE_SINGLES && fFragments.length == 1) {
			return fFragments[0].findBindingsForContentAssist(prefix, filescope, filter, monitor);
		} else {
			List<IIndexBinding[]> result = new ArrayList<IIndexBinding[]>();
			ILinkage[] linkages = Linkage.getIndexerLinkages();
			for (ILinkage linkage : linkages) {
				if (filter.acceptLinkage(linkage)) {
					IIndexFragmentBinding[][] fragmentBindings = new IIndexFragmentBinding[fFragments.length][];
					for (int i = 0; i < fFragments.length; i++) {
						try {
							IBinding[] part = fFragments[i].findBindingsForContentAssist(prefix,
									filescope, retargetFilter(linkage, filter), monitor);
							fragmentBindings[i] = new IIndexFragmentBinding[part.length];
							System.arraycopy(part, 0, fragmentBindings[i], 0, part.length);
						} catch (CoreException e) {
							CCorePlugin.log(e);
							fragmentBindings[i] = IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
						}
					}
					ICompositesFactory factory = getCompositesFactory(linkage.getLinkageID());
					result.add(factory.getCompositeBindings(fragmentBindings));
				}
			}
			return flatten(result);
		}
	}

	@Override
	public IIndexBinding[] findBindings(char[] name, boolean filescope, IndexFilter filter, IProgressMonitor monitor)
			throws CoreException {
		if (SPECIALCASE_SINGLES && fFragments.length == 1) {
			return fFragments[0].findBindings(name, filescope, filter, monitor);
		} else {
			List<IIndexBinding[]> result = new ArrayList<IIndexBinding[]>();
			ILinkage[] linkages = Linkage.getIndexerLinkages();
			for (ILinkage linkage : linkages) {
				if (filter.acceptLinkage(linkage)) {
					IIndexFragmentBinding[][] fragmentBindings = new IIndexFragmentBinding[fFragments.length][];
					for (int i = 0; i < fFragments.length; i++) {
						try {
							IBinding[] part = fFragments[i].findBindings(name, filescope,
									retargetFilter(linkage, filter), monitor);
							fragmentBindings[i] = new IIndexFragmentBinding[part.length];
							System.arraycopy(part, 0, fragmentBindings[i], 0, part.length);
						} catch (CoreException e) {
							CCorePlugin.log(e);
							fragmentBindings[i] = IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
						}
					}
					ICompositesFactory factory = getCompositesFactory(linkage.getLinkageID());
					result.add(factory.getCompositeBindings(fragmentBindings));
				}
			}
			return flatten(result);
		}
	}

	@Override
	public IIndexMacro[] findMacros(char[] name, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		return findMacros(name, false, true, filter, monitor);
	}

	@Override
	public IIndexMacro[] findMacrosForPrefix(char[] name, IndexFilter filter, IProgressMonitor monitor)
			throws CoreException {
		return findMacros(name, true, false, filter, monitor);
	}

	private IIndexMacro[] findMacros(char[] name, boolean isPrefix, boolean caseSensitive,
			IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		// Macros can be represented multiple times when a header is parsed in c- and c++ context,
		// so there is no special case for indexes with single fragments.
		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}
		List<IIndexMacro> result = new ArrayList<IIndexMacro>();
		HashSet<IIndexFileLocation> handledIFLs= new HashSet<IIndexFileLocation>();
		monitor.beginTask(Messages.CIndex_FindBindingsTask_label, fFragments.length);
		for (IIndexFragment fragment : fFragments) {
			HashSet<IIndexFile> allowedFiles= new HashSet<IIndexFile>();
			try {
				IIndexMacro[] macros= fragment.findMacros(name, isPrefix, caseSensitive, filter,
						new SubProgressMonitor(monitor, 1));
				for (IIndexMacro indexMacro : macros) {
					IIndexFile file= indexMacro.getFile();
					if (!allowedFiles.contains(file)) {
						if (handledIFLs.add(file.getLocation())) {
							allowedFiles.add(file);
						} else {
							continue;
						}
					}
					result.add(indexMacro);
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		monitor.done();
		return result.toArray(new IIndexMacro[result.size()]);
	}

	public long getCacheHits() {
		long result= 0;
		for (IIndexFragment fragment : fFragments) {
			result += fragment.getCacheHits();
		}
		return result;
	}

	public long getCacheMisses() {
		long result= 0;
		for (IIndexFragment fragment : fFragments) {
			result += fragment.getCacheMisses();
		}
		return result;
	}

	public void resetCacheCounters() {
		for (IIndexFragment fragment : fFragments) {
			fragment.resetCacheCounters();
		}
	}

	protected void clearResultCache() {
		for (IIndexFragment frag : fFragments) {
			frag.clearResultCache();
		}
	}

	@Override
	public IIndexFileSet createFileSet() {
		return new IndexFileSet();
	}

	@Override
	public IIndexFile[] getAllFiles() throws CoreException {
		HashMap<IIndexFileLocation, IIndexFile> result= new HashMap<IIndexFileLocation, IIndexFile>();
		for (IIndexFragment fragment : fFragments) {
			for (IIndexFragmentFile file : fragment.getAllFiles()) {
				if (file.hasContent()) { 
					result.put(file.getLocation(), file);
				}
			}
		}
		return result.values().toArray(new IIndexFile[result.size()]);
	}

	@Override
	public IIndexFile[] getDefectiveFiles() throws CoreException {
		HashMap<IIndexFileLocation, IIndexFile> result= new HashMap<IIndexFileLocation, IIndexFile>();
		for (IIndexFragment fragment : fFragments) {
			for (IIndexFragmentFile file : fragment.getDefectiveFiles()) {
				if (file.hasContent()) { 
					result.put(file.getLocation(), file);
				}
			}
		}
		return result.values().toArray(new IIndexFile[result.size()]);
	}

	@Override
	public IIndexFile[] getFilesWithUnresolvedIncludes() throws CoreException {
		HashMap<IIndexFileLocation, IIndexFile> result= new HashMap<IIndexFileLocation, IIndexFile>();
		for (IIndexFragment fragment : fFragments) {
			for (IIndexFragmentFile file : fragment.getFilesWithUnresolvedIncludes()) {
				if (file.hasContent()) { 
					result.put(file.getLocation(), file);
				}
			}
		}
		return result.values().toArray(new IIndexFile[result.size()]);
	}

	@Override
	public IIndexScope[] getInlineNamespaces() throws CoreException {
		if (SPECIALCASE_SINGLES && fFragments.length == 1) {
			return fFragments[0].getInlineNamespaces();
		}

		IIndexFragmentBinding[][] preresult = new IIndexFragmentBinding[fFragments.length][];
		for (int i = 0; i < fFragments.length; i++) {
			IIndexScope[] raw = fFragments[i].getInlineNamespaces();
			IIndexFragmentBinding[] arr = preresult[i] = new IIndexFragmentBinding[raw.length];
			for (int j = 0; j < raw.length; j++) {
				arr[j]= (IIndexFragmentBinding) raw[j].getScopeBinding();
			}
		}
		IIndexBinding[] compBinding =
				getCompositesFactory(ILinkage.CPP_LINKAGE_ID).getCompositeBindings(preresult);
		IIndexScope[] result = new IIndexScope[compBinding.length];
		for (int i = 0; i < result.length; i++) {
			result[i]= (IIndexScope) ((ICPPNamespace) compBinding[i]).getNamespaceScope();
		}
		return result;
	}

	@Override
	public boolean isFullyInitialized() {
		for (IIndexFragment fragment : fFragments) {
			if (!fragment.isFullyInitialized())
				return false;
		}
		return true;
	}

	/**
	 * A key used to uniquely identify an IIndexFragmentName object. Uniqueness is guaranteed only
	 * for names corresponding to the same binding.
	 */
	private static final class NameKey {
		private final IIndexFileLocation location;
		private final int linkageID;
		private final int offset;
		private final boolean inWritableFragment;

		NameKey(IIndexFragmentName name, boolean writable) throws CoreException {
			IIndexFile file = name.getFile();
			location = file.getLocation();
			linkageID = file.getLinkageID();
			offset = name.getNodeOffset();
			inWritableFragment= writable;
		}

		@Override
		public int hashCode() {
			return location.hashCode() * 31 + linkageID;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NameKey other = (NameKey) obj;
			// Offset is significant only if matches are from same kind of fragment
			if (inWritableFragment == other.inWritableFragment && offset != other.offset)
				return false;
			if (linkageID != other.linkageID)
				return false;
			if (!location.equals(other.location))
				return false;
			return true;
		}
	}
}
