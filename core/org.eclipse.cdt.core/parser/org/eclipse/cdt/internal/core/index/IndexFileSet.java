/*******************************************************************************
 * Copyright (c) 2008, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.PDOMFileSet;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.IRecordIterator;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

public class IndexFileSet implements IIndexFileSet {
	public static boolean sDEBUG; // Initialized in the PDOMManager.

	private IIndexFileSet fInverse;
	private final HashMap<IIndexFragment, IIndexFragmentFileSet> fSubSets = new HashMap<>();
	private final Map<IBinding, Boolean> fDeclarationContainmentCache = new HashMap<>();
	private long timingContainsDeclarationNanos;

	public IndexFileSet() {
	}

	@Override
	public void add(IIndexFile indexFile) {
		final IIndexFragmentFile fragFile = (IIndexFragmentFile) indexFile;
		final IIndexFragment frag = fragFile.getIndexFragment();
		IIndexFragmentFileSet subSet = fSubSets.get(frag);
		if (subSet == null) {
			subSet = frag.createFileSet();
			fSubSets.put(frag, subSet);
		}
		subSet.add(fragFile);
		fDeclarationContainmentCache.clear();
	}

	@Override
	public void remove(IIndexFile indexFile) {
		final IIndexFragmentFile fragmentFile = (IIndexFragmentFile) indexFile;
		final IIndexFragment fragment = fragmentFile.getIndexFragment();
		IIndexFragmentFileSet subSet = fSubSets.get(fragment);
		if (subSet != null) {
			subSet.remove(fragmentFile);
			fDeclarationContainmentCache.clear();
		}
	}

	@Override
	public boolean containsDeclaration(IIndexBinding binding) {
		Boolean cachedValue = fDeclarationContainmentCache.get(binding);
		if (cachedValue != null)
			return cachedValue;

		long startTime = sDEBUG ? System.nanoTime() : 0;
		boolean contains = computeContainsDeclaration(binding);
		fDeclarationContainmentCache.put(binding, contains);
		if (sDEBUG) {
			timingContainsDeclarationNanos += System.nanoTime() - startTime;
		}
		return contains;
	}

	private boolean computeContainsDeclaration(IIndexBinding binding) {
		int iterationCount = 0;
		for (Map.Entry<IIndexFragment, IIndexFragmentFileSet> entry : fSubSets.entrySet()) {
			IIndexFragment fragment = entry.getKey();
			IIndexFragmentFileSet fragmentFileSet = entry.getValue();
			try {
				if (!fragmentFileSet.isEmpty() && fragmentFileSet instanceof PDOMFileSet && fragment instanceof PDOM) {
					PDOM pdom = (PDOM) fragment;
					PDOMFileSet pdomFileSet = (PDOMFileSet) fragmentFileSet;
					Database db = pdom.getDB();
					IRecordIterator nameIterator = pdom.getDeclarationsDefintitionsRecordIterator(binding);
					Set<Long> visited = null;
					long nameRecord;
					while ((nameRecord = nameIterator.next()) != 0) {
						if (visited != null && !visited.add(nameRecord)) {
							// Cycle detected!
							logInvalidNameChain(pdom, binding);
							return false;
						}
						long fileRecord = PDOMName.getFileRecord(db, nameRecord);
						if (pdomFileSet.containsFile(fileRecord)) {
							if (sDEBUG && iterationCount >= 200) {
								System.out.println(String.format("IndexFileSet: %s (%s) found after %d iterations", //$NON-NLS-1$
										String.join("::", binding.getQualifiedName()), //$NON-NLS-1$
										binding.getClass().getSimpleName(), iterationCount));
							}
							return true;
						}
						if (iterationCount >= 1000 && visited == null) {
							// Iteration count is suspiciously high. Start keeping track of visited names
							// to be able to detect a cycle.
							visited = new HashSet<>();
							visited.add(nameRecord);
						}
					}
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		if (sDEBUG && iterationCount >= 200) {
			System.out.println(String.format("IndexFileSet: %s (%s) not found after %d iterations", //$NON-NLS-1$
					String.join("::", binding.getQualifiedName()), //$NON-NLS-1$
					binding.getClass().getSimpleName(), iterationCount));
		}
		return false;
	}

	private void logInvalidNameChain(PDOM pdom, IIndexBinding binding) {
		try {
			PDOMBinding pdomBinding = (PDOMBinding) binding;
			for (String listType : new String[] { "declarations", "definitions" }) { //$NON-NLS-1$//$NON-NLS-2$
				StringBuilder nameChain = new StringBuilder();
				Set<PDOMName> visited = new HashSet<>();
				PDOMName first = listType.equals("declarations") ? pdomBinding.getFirstDeclaration() //$NON-NLS-1$
						: pdomBinding.getFirstDefinition();
				for (PDOMName name = first; name != null; name = name.getNextInBinding()) {
					if (nameChain.length() != 0)
						nameChain.append('\n');
					nameChain.append(String.format("%s @%d", String.valueOf(name.getSimpleID()), name.getRecord())); //$NON-NLS-1$
					PDOMBinding nameBinding = name.getBinding();
					if (!nameBinding.equals(binding)) {
						nameChain.append(String.format(" belongs to %s (%s) @%d", //$NON-NLS-1$
								String.join("::", nameBinding.getQualifiedName()), //$NON-NLS-1$
								nameBinding.getClass().getSimpleName(), ((PDOMNode) nameBinding).getRecord()));
					}
					if (!visited.add(name)) {
						CCorePlugin.log(String.format("IndexFileSet: %s (%s) @%d - list of %s contains a cycle:\n%s", //$NON-NLS-1$
								String.join("::", binding.getQualifiedName()), //$NON-NLS-1$
								binding.getClass().getSimpleName(), pdomBinding.getRecord(), listType,
								nameChain.toString()));
						return;
					}
				}
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	public long getTimingContainsDeclarationNanos() {
		return timingContainsDeclarationNanos;
	}

	@Override
	public boolean containsNonLocalDeclaration(IBinding binding, IIndexFragment ignore) {
		for (Map.Entry<IIndexFragment, IIndexFragmentFileSet> entry : fSubSets.entrySet()) {
			try {
				final IIndexFragment fragment = entry.getKey();
				final IIndexFragmentFileSet subset = entry.getValue();
				if (fragment != ignore) {
					IIndexFragmentName[] names = fragment.findNames(binding,
							IIndexFragment.FIND_DECLARATIONS_DEFINITIONS | IIndexFragment.FIND_NON_LOCAL_ONLY);
					for (IIndexFragmentName name : names) {
						try {
							if (subset.contains((IIndexFragmentFile) name.getFile())) {
								return true;
							}
						} catch (CoreException e) {
							CCorePlugin.log(e);
						}
					}
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return false;
	}

	public boolean containsNonLocalDeclaration(IBinding binding, IIndexFileLocation ignore) {
		for (Map.Entry<IIndexFragment, IIndexFragmentFileSet> entry : fSubSets.entrySet()) {
			try {
				final IIndexFragment fragment = entry.getKey();
				final IIndexFragmentFileSet subset = entry.getValue();
				IIndexFragmentName[] names = fragment.findNames(binding,
						IIndexFragment.FIND_DECLARATIONS_DEFINITIONS | IIndexFragment.FIND_NON_LOCAL_ONLY);
				for (IIndexFragmentName name : names) {
					try {
						IIndexFile file = name.getFile();
						if (!file.getLocation().equals(ignore) && subset.contains((IIndexFragmentFile) file)) {
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

	@Override
	public IBinding[] filterFileLocalBindings(IBinding[] bindings) {
		return filterFileLocalBindings(bindings, false);
	}

	public IBinding[] filterFileLocalBindings(IBinding[] bindings, boolean invert) {
		if (bindings == null || bindings.length == 0) {
			return bindings;
		}
		BitSet ok = new BitSet(bindings.length);
		if (invert) {
			ok.set(0, bindings.length);
		}

		for (int i = 0; i < bindings.length; i++) {
			IBinding binding = bindings[i];
			if (binding != null) {
				IIndexFragmentBinding fb;
				if (binding instanceof IIndexFragmentBinding) {
					fb = (IIndexFragmentBinding) binding;
				} else {
					fb = binding.getAdapter(IIndexFragmentBinding.class);
				}
				try {
					if (fb != null && fb.isFileLocal()) {
						IIndexFragmentFileSet subSet = fSubSets.get(fb.getFragment());
						if (subSet != null && subSet.containsFileOfLocalBinding(fb)) {
							ok.set(i);
						}
					} else {
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

		IBinding[] result = new IBinding[cardinality];
		int j = ok.nextSetBit(0);
		for (int i = 0; i < result.length; i++) {
			result[i] = bindings[j];
			j = ok.nextSetBit(j + 1);
		}
		return result;
	}

	@Override
	public boolean contains(IIndexFile file) throws CoreException {
		return contains(file, false);
	}

	public boolean contains(IIndexFile file, boolean invert) throws CoreException {
		if (!(file instanceof IIndexFragmentFile))
			return invert;

		IIndexFragmentFile fragmentFile = (IIndexFragmentFile) file;
		IIndexFragmentFileSet subSet = fSubSets.get(fragmentFile.getIndexFragment());
		if (subSet != null && subSet.contains(fragmentFile)) {
			return !invert;
		}
		return invert;
	}

	@Override
	public IIndexFileSet invert() {
		if (fInverse == null) {
			fInverse = new IIndexFileSet() {
				@Override
				public IIndexFileSet invert() {
					return IndexFileSet.this;
				}

				@Override
				public IBinding[] filterFileLocalBindings(IBinding[] bindings) {
					return IndexFileSet.this.filterFileLocalBindings(bindings, true);
				}

				@Override
				public boolean containsDeclaration(IIndexBinding binding) {
					throw new UnsupportedOperationException();
				}

				@Override
				public boolean containsNonLocalDeclaration(IBinding binding, IIndexFragment ignore) {
					throw new UnsupportedOperationException();
				}

				@Override
				public boolean contains(IIndexFile file) throws CoreException {
					return IndexFileSet.this.contains(file, true);
				}

				@Override
				public void add(IIndexFile indexFile) {
					Assert.isLegal(false);
				}

				@Override
				public void remove(IIndexFile indexFile) {
					Assert.isLegal(false);
				}
			};
		}
		return fInverse;
	}
}
