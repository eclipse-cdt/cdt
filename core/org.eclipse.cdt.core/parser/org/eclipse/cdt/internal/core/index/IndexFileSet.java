/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.index;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

public class IndexFileSet implements IIndexFileSet {
	private IIndexFileSet fInverse;
	private HashMap<IIndexFragment, IIndexFragmentFileSet> fSubSets= new HashMap<IIndexFragment, IIndexFragmentFileSet>();

	public IndexFileSet() {
	}
	
	public void add(IIndexFile indexFile) {
		final IIndexFragmentFile fragFile = (IIndexFragmentFile) indexFile;
		final IIndexFragment frag= fragFile.getIndexFragment();
		IIndexFragmentFileSet subSet= fSubSets.get(frag);		
		if (subSet == null) {
			subSet= frag.createFileSet();
			fSubSets.put(frag, subSet);
		}
		subSet.add(fragFile);
	}

	public boolean containsDeclaration(IIndexBinding binding) {
		return containsDeclaration(binding, false);
	}

	boolean containsDeclaration(IIndexBinding binding, boolean inverse) {
		for (Map.Entry<IIndexFragment, IIndexFragmentFileSet> entry : fSubSets.entrySet()) {
			try {
				IIndexFragmentName[] names =
						entry.getKey().findNames(binding, IIndexFragment.FIND_DECLARATIONS_DEFINITIONS);
				for (IIndexFragmentName name : names) {
					try {
						final boolean foundDecl = entry.getValue().contains((IIndexFragmentFile) name.getFile());
						if (foundDecl != inverse) {
							return true;
						}
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return false;
	}

	public IBinding[] filterFileLocalBindings(IBinding[] bindings) {
		return filterFileLocalBindings(bindings, false);
	}
	
	public IBinding[] filterFileLocalBindings(IBinding[] bindings, boolean invert) {
		if (bindings == null || bindings.length == 0) {
			return bindings;
		}
		BitSet ok= new BitSet(bindings.length);
		if (invert) {
			ok.set(0, bindings.length);
		}
		
		for (int i = 0; i < bindings.length; i++) {
			IBinding binding = bindings[i];
			if (binding != null) {
				IIndexFragmentBinding fb;
				if (binding instanceof IIndexFragmentBinding) {
					fb= (IIndexFragmentBinding) binding;
				}
				else {
					fb= (IIndexFragmentBinding) binding.getAdapter(IIndexFragmentBinding.class);
				}
				try {
					if (fb != null && fb.isFileLocal()) {
						IIndexFragmentFileSet subSet= fSubSets.get(fb.getFragment());
						if (subSet != null && subSet.containsFileOfLocalBinding(fb)) {
							ok.set(i);
						}
					}
					else {
						ok.set(i);
					}
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
			}
		}
		
		if (invert) {
			ok.flip(0, bindings.length);
		}
		final int cardinality = ok.cardinality();
		if (cardinality == bindings.length) {
			return bindings;
		}

		IBinding[] result= new IBinding[cardinality];
		int j= ok.nextSetBit(0);
		for (int i = 0; i < result.length; i++) {
			result[i]= bindings[j];
			j= ok.nextSetBit(j+1);
		}
		return result;
	}

	public boolean contains(IIndexFile file) throws CoreException {
		return contains(file, false);
	}
	
	public boolean contains(IIndexFile file, boolean invert) throws CoreException {
		if (!(file instanceof IIndexFragmentFile))
			return invert;
		
		IIndexFragmentFile ifile= (IIndexFragmentFile) file;
		IIndexFragmentFileSet subSet= fSubSets.get(ifile.getIndexFragment());
		if (subSet != null && subSet.contains(ifile)) {
			return !invert;
		}
		return invert;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.IIndexFileSet#invert()
	 */
	public IIndexFileSet invert() {
		if (fInverse == null) {
			fInverse= new IIndexFileSet() {
				public IIndexFileSet invert() {
					return IndexFileSet.this;
				}
				
				public IBinding[] filterFileLocalBindings(IBinding[] bindings) {
					return IndexFileSet.this.filterFileLocalBindings(bindings, true);
				}
				
				public boolean containsDeclaration(IIndexBinding binding) {
					return IndexFileSet.this.containsDeclaration(binding, true);
				}
				
				public boolean contains(IIndexFile file) throws CoreException {
					return IndexFileSet.this.contains(file, true);
				}
				
				public void add(IIndexFile indexFile) {
					Assert.isLegal(false);
				}
			};
		}
		return fInverse;
	}
}
