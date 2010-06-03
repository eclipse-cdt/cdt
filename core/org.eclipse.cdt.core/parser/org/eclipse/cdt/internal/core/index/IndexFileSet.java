/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
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
import org.eclipse.core.runtime.CoreException;

public class IndexFileSet implements IIndexFileSet {

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
		for (Map.Entry<IIndexFragment, IIndexFragmentFileSet> entry : fSubSets.entrySet()) {
			try {
				IIndexFragmentName[] names =
						entry.getKey().findNames(binding, IIndexFragment.FIND_DECLARATIONS_DEFINITIONS);
				for (IIndexFragmentName name : names) {
					try {
						if (entry.getValue().contains((IIndexFragmentFile) name.getFile())) {
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
		if (bindings == null || bindings.length == 0) {
			return bindings;
		}
		BitSet ok= new BitSet(bindings.length);
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
		if (ok.cardinality() == bindings.length) {
			return bindings;
		}
		IBinding[] result= new IBinding[ok.cardinality()];
		int j= ok.nextSetBit(0);
		for (int i = 0; i < result.length; i++) {
			result[i]= bindings[j];
			j= ok.nextSetBit(j+1);
		}
		return result;
	}

	public boolean contains(IIndexFile file) throws CoreException {
		if (!(file instanceof IIndexFragmentFile))
			return false;
		
		IIndexFragmentFile ifile= (IIndexFragmentFile) file;
		IIndexFragmentFileSet subSet= fSubSets.get(ifile.getIndexFragment());
		if (subSet != null) {
			return subSet.contains(ifile);
		}
		return false;
	}
}
