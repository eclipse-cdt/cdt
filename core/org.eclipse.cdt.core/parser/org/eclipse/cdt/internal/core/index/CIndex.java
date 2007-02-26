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
 *    Anton Leherbauer (Wind River Systems)
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.index.composite.CompositingNotImplementedError;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;
import org.eclipse.cdt.internal.core.index.composite.c.CCompositesFactory;
import org.eclipse.cdt.internal.core.index.composite.cpp.CPPCompositesFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
			if(SPECIALCASE_SINGLES && fFragments.length==1) {
				return fFragments[0].findBinding((IASTName) name);
			} else {
				for (int i = 0; i < fPrimaryFragmentCount; i++) {
					IIndexFragmentBinding binding= fFragments[i].findBinding((IASTName) name);
					if(binding!=null) {
						return getCompositesFactory(binding.getLinkage().getID()).getCompositeBinding(binding);
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
		if(SPECIALCASE_SINGLES && fFragments.length==1) {
			 return fFragments[0].findBindings(patterns, isFullyQualified, filter, monitor); 
		} else {
			List result = new ArrayList();
			ILinkage[] linkages = Linkage.getAllLinkages();
			for(int j=0; j < linkages.length; j++) {
				if(filter.acceptLinkage(linkages[j])) {
					IIndexFragmentBinding[][] fragmentBindings = new IIndexFragmentBinding[fPrimaryFragmentCount][];
					for (int i = 0; i < fPrimaryFragmentCount; i++) {
						try {
							IBinding[] part = fFragments[i].findBindings(patterns, isFullyQualified, retargetFilter(linkages[j], filter), new SubProgressMonitor(monitor, 1));
							fragmentBindings[i] = new IIndexFragmentBinding[part.length];
							System.arraycopy(part, 0, fragmentBindings[i], 0, part.length);
						} catch (CoreException e) {
							CCorePlugin.log(e);
							fragmentBindings[i] = IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
						}
					}
					ICompositesFactory factory = getCompositesFactory(linkages[j].getID());
					result.add(factory.getCompositeBindings(fragmentBindings));
				}
			}
			return flatten(result);
		}
	}

	public IIndexName[] findNames(IBinding binding, int flags) throws CoreException {
		ArrayList result= new ArrayList();
		for (int i = 0; i < fPrimaryFragmentCount; i++) {
			IIndexFragmentBinding adaptedBinding= fFragments[i].adaptBinding(binding);
			if (adaptedBinding != null) {
				result.addAll(Arrays.asList(fFragments[i].findNames(adaptedBinding, flags)));
			}
		}
		return (IIndexName[]) result.toArray(new IIndexName[result.size()]);
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

	public IIndexFile getFile(IIndexFileLocation location) throws CoreException {
		IIndexFile result= null, backup= null;
		for (int i = 0; result==null && i < fPrimaryFragmentCount; i++) {
			IIndexFragmentFile candidate= fFragments[i].getFile(location);
			if(candidate!=null) {
				if(candidate.hasNames()) {
					result = candidate;
				}
				if(backup==null)
					backup = candidate;
			}
		}
		return result == null ? backup : result;
	}

	public IIndexFile resolveInclude(IIndexInclude include) throws CoreException {
		if (!include.isResolved()) {
			return null;
		}
		IIndexFragmentInclude fragmentInclude = (IIndexFragmentInclude) include;
		IIndexFragment frag= fragmentInclude.getFragment();
		if (isPrimaryFragment(frag)) {
			IIndexFile result= fragmentInclude.getIncludes();
			if (result != null) {
				return result;
			}
		}

		IIndexFileLocation location= include.getIncludesLocation();
		for (int i = 0; i < fPrimaryFragmentCount; i++) {
			IIndexFragment otherFrag = fFragments[i];
			if (otherFrag != frag) {
				IIndexFile result= otherFrag.getFile(location);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	public IIndexInclude[] findIncludedBy(IIndexFile file) throws CoreException {
		return findIncludedBy(file, 0);
	}	

	public IIndexInclude[] findIncludedBy(IIndexFile file, int depth) throws CoreException {
		List result= new ArrayList();
		findIncludedBy(Collections.singletonList(file), result, depth, new HashSet());
		return (IIndexInclude[]) result.toArray(new IIndexInclude[result.size()]);
	}

	public void findIncludedBy(List in, List out, int depth, HashSet handled) throws CoreException {
		List nextLevel= depth != 0 ? new LinkedList() : null;
		for (Iterator it= in.iterator(); it.hasNext(); ) {
			IIndexFragmentFile file = (IIndexFragmentFile) it.next();
			for (int j= 0; j < fPrimaryFragmentCount; j++) {
				IIndexInclude[] includedBy= fFragments[j].findIncludedBy(file);
				for (int k= 0; k < includedBy.length; k++) {
					IIndexInclude include = includedBy[k];
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
		List result= new ArrayList();
		findIncludes(Collections.singletonList(file), result, depth, new HashSet());
		return (IIndexInclude[]) result.toArray(new IIndexInclude[result.size()]);
	}

	private void findIncludes(List in, List out, int depth, HashSet handled) throws CoreException {
		List nextLevel= depth != 0 ? new LinkedList() : null;
		for (Iterator it= in.iterator(); it.hasNext(); ) {
			IIndexFragmentFile file = (IIndexFragmentFile) it.next();
			IIndexFragment frag= file.getIndexFragment();
			if (isPrimaryFragment(frag)) {
				IIndexInclude[] includes= file.getIncludes();
				for (int k= 0; k < includes.length; k++) {
					IIndexInclude include = includes[k];
					if (handled.add(include.getIncludesLocation())) {
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
			}
			finally {
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
			for (int i=0; i < fFragments.length; i++) {
				fFragments[i].releaseReadLock();
			}
		}
	}

	protected synchronized int getReadLockCount() {
		return fReadLock;
	}

	public long getLastWriteAccess() {
		long result= 0;
		for (int i=0; i < fFragments.length; i++) {
			result= Math.max(result, fFragments[i].getLastWriteAccess());
		}
		return result;
	}

	public IIndexBinding[] findBindings(char[][] names, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		if(SPECIALCASE_SINGLES && fFragments.length==1) {
			try {
				return fFragments[0].findBindings(names, filter, monitor);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
			}
		} else {
			List result = new ArrayList();
			ILinkage[] linkages = Linkage.getAllLinkages();
			monitor.beginTask(Messages.CIndex_FindBindingsTask_label, fFragments.length * linkages.length);
			for(int j=0; j < linkages.length; j++) {
				if(filter.acceptLinkage(linkages[j])) {
					IIndexFragmentBinding[][] fragmentBindings = new IIndexFragmentBinding[fPrimaryFragmentCount][];
					for (int i = 0; i < fPrimaryFragmentCount; i++) {
						try {
							IBinding[] part = fFragments[i].findBindings(names, retargetFilter(linkages[j], filter), new SubProgressMonitor(monitor, 1));
							fragmentBindings[i] = new IIndexFragmentBinding[part.length];
							System.arraycopy(part, 0, fragmentBindings[i], 0, part.length);
						} catch (CoreException e) {
							CCorePlugin.log(e);
							fragmentBindings[i] = IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
						}
					}
					ICompositesFactory factory = getCompositesFactory(linkages[j].getID());
					result.add(factory.getCompositeBindings(fragmentBindings));
				}
			}
			monitor.done();
			return flatten(result);
			
		}
	}

	public IIndexBinding adaptBinding(IBinding binding) {
		try {
			if(SPECIALCASE_SINGLES && fFragments.length==1) {
				return fFragments[0].adaptBinding(binding);
			} else {
				return getCompositesFactory(binding.getLinkage().getID()).getCompositeBinding(binding);
			}
		} catch(CoreException ce) {
			CCorePlugin.log(ce);
			return null;
		}
	}

	public IIndexBinding[] findBindings(char[] name, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		return findBindings(new char[][]{name}, filter, monitor);
	}

	/*
	 * Non-API
	 */
	
	private IIndexBinding[] flatten(List bindingArrays) {
		int size = 0;
		for(int i=0; i<bindingArrays.size(); i++) {
			size += ((IBinding[])bindingArrays.get(i)).length;
		}
		IIndexBinding[] result = new IIndexBinding[size];
		int offset = 0;
		for(int i=0; i<bindingArrays.size(); i++) {
			IBinding[] src = (IBinding[]) bindingArrays.get(i);
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
	
	private boolean isPrimaryFragment(IIndexFragment frag) {
		for (int i = 0; i < fPrimaryFragmentCount; i++) {
			if (frag == fFragments[i]) {
				return true;
			}
		}
		return false;
	}
	
	public IIndexFragmentBinding[] findEquivalentBindings(IBinding binding) throws CoreException {
		List result = new ArrayList();
		for (int i = 0; i < fFragments.length; i++) {
			IIndexFragmentBinding adapted = fFragments[i].adaptBinding(binding);
			if (adapted != null) {
				result.add(adapted);
			}
		}
		return (IIndexFragmentBinding[]) result.toArray(new IIndexFragmentBinding[result.size()]);
	}
	
	ICompositesFactory cppCF, cCF, fCF;
	private ICompositesFactory getCompositesFactory(String linkageID) {
		if(linkageID.equals(ILinkage.CPP_LINKAGE_ID)) {
			if(cppCF==null) {
				cppCF = new CPPCompositesFactory(new CIndex(fFragments, fFragments.length));
			}
			return cppCF; 
		}
		if(linkageID.equals(ILinkage.C_LINKAGE_ID)) {
			if(cCF==null) {
				cCF = new CCompositesFactory(new CIndex(fFragments, fFragments.length));
			}
			return cCF;
		}
		if(linkageID.equals(ILinkage.FORTRAN_LINKAGE_ID)) {
			if(fCF==null) {
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
			public boolean acceptBinding(IBinding binding) {
				return filter.acceptBinding(binding);
			}
			public boolean acceptImplicitMethods() {
				return filter.acceptImplicitMethods();
			};
			public boolean acceptLinkage(ILinkage other) {
				return linkage.getID().equals(other.getID());
			}
		};
	}
	
	public IIndexBinding[] findBindingsForPrefix(char[] prefix, boolean filescope, IndexFilter filter) throws CoreException {
		if(SPECIALCASE_SINGLES && fFragments.length==1) {
			return fFragments[0].findBindingsForPrefix(prefix, filescope, filter);
		} else {
			List result = new ArrayList();
			ILinkage[] linkages = Linkage.getAllLinkages();
			for(int j=0; j < linkages.length; j++) {
				if(filter.acceptLinkage(linkages[j])) {
					IIndexFragmentBinding[][] fragmentBindings = new IIndexFragmentBinding[fPrimaryFragmentCount][];
					for (int i = 0; i < fPrimaryFragmentCount; i++) {
						try {
							IBinding[] part = fFragments[i].findBindingsForPrefix(prefix, filescope, retargetFilter(linkages[j], filter));
							fragmentBindings[i] = new IIndexFragmentBinding[part.length];
							System.arraycopy(part, 0, fragmentBindings[i], 0, part.length);
						} catch (CoreException e) {
							CCorePlugin.log(e);
							fragmentBindings[i] = IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
						}
					}
					ICompositesFactory factory = getCompositesFactory(linkages[j].getID());
					result.add(factory.getCompositeBindings(fragmentBindings));
				}
			}
			return flatten(result);
		}
	}
}
