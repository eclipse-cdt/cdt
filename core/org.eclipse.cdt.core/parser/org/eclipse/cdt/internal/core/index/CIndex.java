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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

public class CIndex implements IIndex {
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

	public IIndexFragment[] getPrimaryFragments() {
		IIndexFragment[] result= new IIndexFragment[fPrimaryFragmentCount];
		System.arraycopy(fFragments, 0, result, 0, fPrimaryFragmentCount);
		return result;
	}
	
	public IIndexBinding adaptBinding(IBinding binding) throws CoreException {
		if (binding instanceof IIndexFragmentBinding) {
			IIndexFragmentBinding fragBinding= (IIndexFragmentBinding) binding;
			if (isFragment(fragBinding.getFragment())) {
				return fragBinding;
			}
		}
		
		for (int i = 0; i < fFragments.length; i++) {
			IIndexProxyBinding result= fFragments[i].adaptBinding(binding);
			if (result instanceof IIndexFragmentBinding) {
				return (IIndexFragmentBinding) result;
			}
		}
		return null;
	}
	
	public IIndexBinding findBinding(IName name) throws CoreException {
		if (name instanceof IIndexFragmentName) {
			return findBinding((IIndexFragmentName) name);
		}
		if (name instanceof IASTName) {
			return findBinding((IASTName) name);
		}
		return null;
	}

	private IIndexBinding findBinding(IIndexFragmentName indexName) throws CoreException {
		IIndexProxyBinding proxy= indexName.getBinding();
		
		if (proxy instanceof IIndexFragmentBinding) {
			IIndexFragmentBinding binding= (IIndexFragmentBinding) proxy;
			if (isFragment(binding.getFragment())) {
				return binding;
			}
		}
		
		if (proxy != null) {
			for (int i = 0; i < fFragments.length; i++) {
				IIndexProxyBinding result= fFragments[i].adaptBinding(proxy);
				if (result instanceof IIndexFragmentBinding) {
					return (IIndexFragmentBinding) result;
				}
			}
		}
		return null;
	}

	private boolean isFragment(IIndexFragment frag) {
		for (int i = 0; i < fFragments.length; i++) {
			if (frag == fFragments[i]) {
				return true;
			}
		}
		return false;
	}

	private boolean isPrimaryFragment(IIndexFragment frag) {
		for (int i = 0; i < fPrimaryFragmentCount; i++) {
			if (frag == fFragments[i]) {
				return true;
			}
		}
		return false;
	}

	private IIndexBinding findBinding(IASTName astName) throws CoreException {
		IIndexProxyBinding binding= null;
		for (int i = 0; i < fFragments.length; i++) {
			if (binding == null) {
				binding= fFragments[i].findBinding(astName);
				if (binding instanceof IIndexFragmentBinding) {
					return (IIndexFragmentBinding) binding;
				}
			}
			else {
				IIndexProxyBinding alt= fFragments[i].adaptBinding(binding);
				if (alt instanceof IIndexFragmentBinding) {
					return (IIndexFragmentBinding) alt;
				}
			}
		}
		return null;
	}
	
	public IIndexBinding[] findBindings(Pattern pattern, boolean isFullyQualified, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		return findBindings(new Pattern[]{pattern}, isFullyQualified, filter, monitor);
	}
	
	public IIndexBinding[] findBindings(Pattern[] patterns, boolean isFullyQualified, IndexFilter filter, IProgressMonitor monitor) throws CoreException {
		ArrayList result= new ArrayList();
		monitor.beginTask(Messages.CIndex_FindBindingsTask_label, fFragments.length);
		for (int i = 0; !monitor.isCanceled() && i < fFragments.length; i++) {
			result.addAll(Arrays.asList(fFragments[i].findBindings(patterns, isFullyQualified, filter, new SubProgressMonitor(monitor, 1))));
		}
		monitor.done();
		return (IIndexBinding[]) result.toArray(new IIndexBinding[result.size()]);
	}
	
	public IIndexName[] findNames(IBinding binding, int flags) throws CoreException {
		ArrayList result= new ArrayList();
		for (int i = 0; i < fPrimaryFragmentCount; i++) {
			IIndexProxyBinding adaptedBinding= fFragments[i].adaptBinding(binding);
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
		IIndexFile result= null;
		for (int i = 0; result==null && i < fPrimaryFragmentCount; i++) {
			result= fFragments[i].getFile(location);
		}
		return result;
	}

	public IIndexFile resolveInclude(IIndexInclude include) throws CoreException {
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

	public IBinding[] findInGlobalScope(ILinkage linkage, char[] name) {
		ArrayList result= new ArrayList();
		for (int i = 0; i < fFragments.length; i++) {
			try {
				IBinding[] part = fFragments[i].findInGlobalScope(linkage, name);
				for (int j = 0; j < part.length; j++) {
					IBinding binding = part[j];
					if (binding instanceof IIndexBinding) {
						result.add(binding);
					}
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		if (!result.isEmpty()) {
			return (IIndexBinding[]) result.toArray(new IIndexBinding[result.size()]);
		}
		return IIndexBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	public IBinding[] findInNamespace(IBinding nsbinding, char[] name) {
		ArrayList result= new ArrayList();
		for (int i = 0; i < fFragments.length; i++) {
			try {
				IBinding[] part = fFragments[i].findInNamespace(nsbinding, name);
				for (int j = 0; j < part.length; j++) {
					IBinding binding = part[j];
					if (binding instanceof IIndexBinding) {
						result.add(binding);
					}
				}
				if (!result.isEmpty()) {
					return (IIndexBinding[]) result.toArray(new IIndexBinding[result.size()]);
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return IIndexBinding.EMPTY_INDEX_BINDING_ARRAY;
	}
	
	public IBinding[] findBindingsForPrefix(String prefix, IndexFilter filter) throws CoreException {
		ArrayList result= new ArrayList();
		for (int i = 0; i < fFragments.length; i++) {
			try {
				IBinding[] part = fFragments[i].findBindingsForPrefix(prefix, filter);
				for (int j = 0; j < part.length; j++) {
					IBinding binding = part[j];
					if (binding instanceof IIndexBinding) {
						result.add(binding);
					}
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		if (!result.isEmpty()) {
			return (IIndexBinding[]) result.toArray(new IIndexBinding[result.size()]);
		}
		return IIndexBinding.EMPTY_INDEX_BINDING_ARRAY;
	}
}
