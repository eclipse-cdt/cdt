/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Bryan Wilkinson (QNX)
 *    Andrew Ferguson (Symbian)
 *    Anton Leherbauer (Wind River Systems)
 *    Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
	
	final private IIndexFragment[] fFragments;
	final private int fPrimaryFragmentCount;
	private int fReadLock;
	private ICompositesFactory cppCF, cCF, fCF;
	
	public CIndex(IIndexFragment[] fragments, int primaryFragmentCount) {
		fFragments= fragments;
		fPrimaryFragmentCount= primaryFragmentCount;
	}

	public CIndex(IIndexFragment[] fragments) {
		this(fragments, fragments.length);
	}

	public IIndexBinding findBinding(IName name) throws CoreException {
		if (name instanceof IIndexFragmentName) {
			return adaptBinding(((IIndexFragmentName) name).getBinding());
		} else if (name instanceof IASTName) {
			if (SPECIALCASE_SINGLES && fFragments.length == 1) {
				return fFragments[0].findBinding((IASTName) name);
			} else {
				for (int i = 0; i < fPrimaryFragmentCount; i++) {
					IIndexFragmentBinding binding= fFragments[i].findBinding((IASTName) name);
					if (binding!=null) {
						return getCompositesFactory(binding.getLinkage().getLinkageID()).getCompositeBinding(binding);
					}
				}
			}
		}
		return null;
	}
	
	public IIndexBinding[] findBindings(Pattern pattern, boolean isFullyQualified, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		return findBindings(new Pattern[]{pattern}, isFullyQualified, filter, monitor);
	}
	
	public IIndexBinding[] findBindings(Pattern[] patterns, boolean isFullyQualified, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		if (SPECIALCASE_SINGLES && fFragments.length == 1) {
			 return fFragments[0].findBindings(patterns, isFullyQualified, filter, monitor); 
		} else {
			List<IIndexBinding[]> result = new ArrayList<IIndexBinding[]>();
			ILinkage[] linkages = Linkage.getIndexerLinkages();
			for (ILinkage linkage : linkages) {
				if (filter.acceptLinkage(linkage)) {
					IIndexFragmentBinding[][] fragmentBindings = new IIndexFragmentBinding[fPrimaryFragmentCount][];
					for (int i = 0; i < fPrimaryFragmentCount; i++) {
						try {
							IBinding[] part = fFragments[i].findBindings(patterns, isFullyQualified, retargetFilter(linkage, filter), monitor);
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

	public IIndexBinding[] findMacroContainers(Pattern pattern, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		if (SPECIALCASE_SINGLES && fFragments.length == 1) {
			 return fFragments[0].findMacroContainers(pattern, filter, monitor); 
		} else {
			List<IIndexBinding[]> result = new ArrayList<IIndexBinding[]>();
			ILinkage[] linkages = Linkage.getIndexerLinkages();
			for (ILinkage linkage : linkages) {
				if (filter.acceptLinkage(linkage)) {
					IIndexFragmentBinding[][] fragmentBindings = new IIndexFragmentBinding[fPrimaryFragmentCount][];
					for (int i = 0; i < fPrimaryFragmentCount; i++) {
						try {
							IBinding[] part = fFragments[i].findMacroContainers(pattern, retargetFilter(linkage, filter), monitor);
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

	public IIndexName[] findNames(IBinding binding, int flags) throws CoreException {
		LinkedList<IIndexFragmentName> result= new LinkedList<IIndexFragmentName>();
		if (binding instanceof ICPPUsingDeclaration) {
			IBinding[] bindings= ((ICPPUsingDeclaration)binding).getDelegates();
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
		
		int fragCount= 0;
		for (int i = 0; i < fPrimaryFragmentCount; i++) {
			final IIndexFragmentName[] names = fFragments[i].findNames(binding, flags);
			if (names.length > 0) {
				result.addAll(Arrays.asList(names));
				fragCount++;
			}
		}
		// bug 192352, files can reside in multiple fragments, remove duplicates
		if (fragCount > 1 || (flags & IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES) != 0) {
			HashMap<String, IIndexFile> fileMap= new HashMap<String, IIndexFile>();
			for (Iterator<IIndexFragmentName> iterator = result.iterator(); iterator.hasNext();) {
				final IIndexFragmentName name = iterator.next();
				final IIndexFile file= name.getFile();
				final String fileKey= name.getFile().getLocation().getURI().toString();
				final IIndexFile otherFile= fileMap.get(fileKey);
				if (otherFile == null) {
					fileMap.put(fileKey, file);
				} else if (!otherFile.equals(file)) { // same file in another fragment
					iterator.remove();
				}
			}
		}
		return result.toArray(new IIndexName[result.size()]);
	}

	public IIndexName[] findDeclarations(IBinding binding) throws CoreException {
		return findNames(binding, FIND_DECLARATIONS_DEFINITIONS);
	}

	public IIndexName[] findDefinitions(IBinding binding) throws CoreException {
		return findNames(binding, FIND_DEFINITIONS);
	}

	public IIndexName[] findReferences(IBinding binding) throws CoreException {
		return findNames(binding, FIND_REFERENCES);
	}

	public IIndexFile getFile(int linkageID, IIndexFileLocation location) throws CoreException {
		for (int i = 0; i < fPrimaryFragmentCount; i++) {
			IIndexFragmentFile candidate= fFragments[i].getFile(linkageID, location);
			if (candidate!=null && candidate.hasContent()) {
				return candidate;
			}
		}
		return null;
	}

	public IIndexFile[] getFiles(IIndexFileLocation location) throws CoreException {
		if (location == null) {
			return IIndexFile.EMPTY_FILE_ARRAY;
		}
		ArrayList<IIndexFragmentFile> result= new ArrayList<IIndexFragmentFile>();
		BitSet linkages= new BitSet();
		for (int i = 0; i < fPrimaryFragmentCount; i++) {
			IIndexFragmentFile[] candidates= fFragments[i].getFiles(location);
			for (IIndexFragmentFile candidate : candidates) {
				int linkage= candidate.getLinkageID();
				if (!linkages.get(linkage) && candidate.hasContent()) {
					result.add(candidate);
					linkages.set(linkage);
				}
			}
		}
		if (result.isEmpty()) {
			return IIndexFile.EMPTY_FILE_ARRAY;
		}
		return result.toArray(new IIndexFile[result.size()]);
	}

	public IIndexFile resolveInclude(IIndexInclude include) throws CoreException {
		if (!include.isResolved()) {
			return null;
		}
		IIndexFragmentInclude fragmentInclude = (IIndexFragmentInclude) include;
		IIndexFragmentFile result= fragmentInclude.getIncludes();
		if (result != null && result.hasContent()) {
			return result;
		}

		return getFile(include.getIncludedBy().getLinkageID(), include.getIncludesLocation());
	}

	public IIndexInclude[] findIncludedBy(IIndexFile file) throws CoreException {
		return findIncludedBy(file, 0);
	}	

	public IIndexInclude[] findIncludedBy(IIndexFile file, int depth) throws CoreException {
		List<IIndexInclude> result= new ArrayList<IIndexInclude>();
		findIncludedBy(Collections.singletonList(file), result, depth, new HashSet<IIndexFileLocation>());
		return result.toArray(new IIndexInclude[result.size()]);
	}

	public void findIncludedBy(List<IIndexFile> in, List<IIndexInclude> out, int depth, HashSet<IIndexFileLocation> handled) throws CoreException {
		List<IIndexFile> nextLevel= depth != 0 ? new LinkedList<IIndexFile>() : null;
		for (IIndexFile iIndexFile : in) {
			IIndexFragmentFile file = (IIndexFragmentFile) iIndexFile;
			for (int j = 0; j < fPrimaryFragmentCount; j++) {
				IIndexInclude[] includedBy= fFragments[j].findIncludedBy(file);
				for (IIndexInclude include : includedBy) {
					if (handled.add(include.getIncludedByLocation())) {
						out.add(include);
						if (depth != 0) {
							nextLevel.add(include.getIncludedBy());
						}
					}
				}
			}
		}
		if (depth == 0 || nextLevel.isEmpty()) {
			return;
		}
		if (depth > 0) {
			depth--;
		}
		findIncludedBy(nextLevel, out, depth, handled);
	}


	public IIndexInclude[] findIncludes(IIndexFile file) throws CoreException {
		return findIncludes(file, 0);
	}

	public IIndexInclude[] findIncludes(IIndexFile file, int depth) throws CoreException {
		List<IIndexInclude> result= new ArrayList<IIndexInclude>();
		findIncludes(Collections.singletonList(file), result, depth, new HashSet<Object>());
		return result.toArray(new IIndexInclude[result.size()]);
	}

	private void findIncludes(List<IIndexFile> in, List<IIndexInclude> out, int depth, HashSet<Object> handled) throws CoreException {
		List<IIndexFile> nextLevel= depth != 0 ? new LinkedList<IIndexFile>() : null;
		for (IIndexFile iIndexFile : in) {
			IIndexFragmentFile file = (IIndexFragmentFile) iIndexFile;
			IIndexInclude[] includes= file.getIncludes();
			for (IIndexInclude include : includes) {
				IIndexFileLocation target= include.getIncludesLocation();
				Object key= target != null ? (Object) target : include.getFullName();
				if (handled.add(key)) {
					out.add(include);
					if (depth != 0) {
						IIndexFile includedByFile= resolveInclude(include);
						if (includedByFile != null) {
							nextLevel.add(includedByFile);
						}
					}
				}
			}
		}
		if (depth == 0 || nextLevel.isEmpty()) {
			return;
		}
		if (depth > 0) {
			depth--;
		}
		findIncludes(nextLevel, out, depth, handled);
	}

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

	public synchronized void releaseReadLock() {
		if (--fReadLock == 0) {
			for (IIndexFragment fFragment : fFragments) {
				fFragment.releaseReadLock();
			}
		}
	}

	protected synchronized int getReadLockCount() {
		return fReadLock;
	}

	public boolean hasWaitingReaders() {
		for (IIndexFragment fFragment : fFragments) {
			if (fFragment.hasWaitingReaders()) {
				return true;
			}
		}
		return false;
	}

	public long getLastWriteAccess() {
		long result= 0;
		for (IIndexFragment fFragment : fFragments) {
			result= Math.max(result, fFragment.getLastWriteAccess());
		}
		return result;
	}

	public IIndexBinding[] findBindings(char[][] names, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
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
					IIndexFragmentBinding[][] fragmentBindings = new IIndexFragmentBinding[fPrimaryFragmentCount][];
					for (int i = 0; i < fPrimaryFragmentCount; i++) {
						try {
							IBinding[] part = fFragments[i].findBindings(names, retargetFilter(linkage, filter), new SubProgressMonitor(monitor, 1));
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
	
	public IIndexBinding adaptBinding(IBinding binding) {
		try {
			if (SPECIALCASE_SINGLES && fFragments.length == 1) {
				return fFragments[0].adaptBinding(binding);
			} else {
				for (int i = 0; i < fPrimaryFragmentCount; i++) {
					IIndexFragmentBinding adaptedBinding= fFragments[i].adaptBinding(binding);
					if (adaptedBinding != null) {
						return getCompositesFactory(binding.getLinkage().getLinkageID()).getCompositeBinding(adaptedBinding);
					}
				}
			}
		} catch(CoreException ce) {
			CCorePlugin.log(ce);
		}
		return null;
	}

	public IIndexBinding[] findBindings(char[] name, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
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
	
	public IIndexFragment[] getPrimaryFragments() {
		IIndexFragment[] result= new IIndexFragment[fPrimaryFragmentCount];
		System.arraycopy(fFragments, 0, result, 0, fPrimaryFragmentCount);
		return result;
	}
		
	public IIndexFragmentBinding[] findEquivalentBindings(IBinding binding) throws CoreException {
		List<IIndexFragmentBinding> result = new ArrayList<IIndexFragmentBinding>();
		for (IIndexFragment fFragment : fFragments) {
			IIndexFragmentBinding adapted = fFragment.adaptBinding(binding);
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
				cppCF = new CPPCompositesFactory(new CIndex(fFragments, fFragments.length));
			}
			return cppCF; 
		
		case ILinkage.C_LINKAGE_ID:
			if (cCF == null) {
				cCF = new CCompositesFactory(new CIndex(fFragments, fFragments.length));
			}
			return cCF;
		
		case ILinkage.FORTRAN_LINKAGE_ID:
			if (fCF == null) {
				fCF = new CCompositesFactory(new CIndex(fFragments, fFragments.length)); 
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
	
	public IIndexBinding[] findBindingsForPrefix(char[] prefix, boolean filescope, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		if (SPECIALCASE_SINGLES && fFragments.length == 1) {
			return fFragments[0].findBindingsForPrefix(prefix, filescope, filter, monitor);
		} else {
			List<IIndexBinding[]> result = new ArrayList<IIndexBinding[]>();
			ILinkage[] linkages = Linkage.getIndexerLinkages();
			for (ILinkage linkage : linkages) {
				if (filter.acceptLinkage(linkage)) {
					IIndexFragmentBinding[][] fragmentBindings = new IIndexFragmentBinding[fPrimaryFragmentCount][];
					for (int i = 0; i < fPrimaryFragmentCount; i++) {
						try {
							IBinding[] part = fFragments[i].findBindingsForPrefix(prefix, filescope, retargetFilter(linkage, filter), monitor);
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

	public IIndexBinding[] findBindings(char[] name, boolean filescope, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		if (SPECIALCASE_SINGLES && fFragments.length == 1) {
			return fFragments[0].findBindings(name, filescope, filter, monitor);
		} else {
			List<IIndexBinding[]> result = new ArrayList<IIndexBinding[]>();
			ILinkage[] linkages = Linkage.getIndexerLinkages();
			for (ILinkage linkage : linkages) {
				if (filter.acceptLinkage(linkage)) {
					IIndexFragmentBinding[][] fragmentBindings = new IIndexFragmentBinding[fPrimaryFragmentCount][];
					for (int i = 0; i < fPrimaryFragmentCount; i++) {
						try {
							IBinding[] part = fFragments[i].findBindings(name, filescope, retargetFilter(linkage, filter), monitor);
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

	public IIndexMacro[] findMacros(char[] name, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		return findMacros(name, false, true, filter, monitor);
	}
	
	public IIndexMacro[] findMacrosForPrefix(char[] name, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		return findMacros(name, true, false, filter, monitor);
	}

	private IIndexMacro[] findMacros(char[] name, boolean isPrefix, boolean caseSensitive, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		// macros can be represented multiple times when a header is parsed in c- and c++ context,
		// so there is no special case for indexes with single fragments.
		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}
		List<IIndexMacro> result = new ArrayList<IIndexMacro>();
		HashSet<IIndexFileLocation> handledIFLs= new HashSet<IIndexFileLocation>();
		monitor.beginTask(Messages.CIndex_FindBindingsTask_label, fFragments.length);
		for (int i = 0; i < fPrimaryFragmentCount; i++) {
			HashSet<IIndexFile> allowedFiles= new HashSet<IIndexFile>();
			try {
				IIndexMacro[] macros= fFragments[i].findMacros(name, isPrefix, caseSensitive, filter, new SubProgressMonitor(monitor, 1));
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
		for (IIndexFragment fFragment : fFragments) {
			result+= fFragment.getCacheHits();
		}
		return result;
	}

	public long getCacheMisses() {
		long result= 0;
		for (IIndexFragment fFragment : fFragments) {
			result+= fFragment.getCacheMisses();
		}
		return result;
	}

	public void resetCacheCounters() {
		for (IIndexFragment fFragment : fFragments) {
			fFragment.resetCacheCounters();
		}
	}
	
	protected void clearResultCache() {
		for (IIndexFragment frag : fFragments) {
			frag.clearResultCache();
		}
	}
	
	public IIndexFileSet createFileSet() {
		return new IndexFileSet();
	}

	public IIndexFile[] getAllFiles() throws CoreException {
		HashMap<IIndexFileLocation, IIndexFile> result= new HashMap<IIndexFileLocation, IIndexFile>();
		for (IIndexFragment ifrag : fFragments) {
			for (IIndexFragmentFile iff : ifrag.getAllFiles()) {
				result.put(iff.getLocation(), iff);
			}
		}
		return result.values().toArray(new IIndexFile[result.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.IIndex#getInlineNamespaces()
	 */
	public IIndexScope[] getInlineNamespaces() throws CoreException {
		if (SPECIALCASE_SINGLES && fFragments.length == 1) {
			return fFragments[0].getInlineNamespaces();
		} 

		IIndexFragmentBinding[][] preresult = new IIndexFragmentBinding[fPrimaryFragmentCount][];
		for (int i = 0; i < fPrimaryFragmentCount; i++) {
			IIndexScope[] raw = fFragments[i].getInlineNamespaces();
			IIndexFragmentBinding[] arr = preresult[i] = new IIndexFragmentBinding[raw.length];
			for (int j=0; j<raw.length; j++) {
				arr[j]= (IIndexFragmentBinding) raw[j].getScopeBinding();
			} 
		}
		IIndexBinding[] compBinding = getCompositesFactory(ILinkage.CPP_LINKAGE_ID).getCompositeBindings(preresult);
		IIndexScope[] result = new IIndexScope[compBinding.length];
		for(int i=0; i<result.length; i++) {
			result[i]= (IIndexScope) ((ICPPNamespace) compBinding[i]).getNamespaceScope();
		}
		return result;
	}
}
