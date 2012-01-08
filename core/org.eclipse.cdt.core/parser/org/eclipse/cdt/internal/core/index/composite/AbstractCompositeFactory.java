/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite;

import java.util.Comparator;
import java.util.TreeSet;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.index.CIndex;
import org.eclipse.cdt.internal.core.index.DefaultFragmentBindingComparator;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBindingComparator;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFragmentBindingComparator;
import org.eclipse.core.runtime.CoreException;

/**
 * Commonality between composite factories
 */
public abstract class AbstractCompositeFactory implements ICompositesFactory {	
	protected IIndex index;
	private Comparator<IIndexFragmentBinding> fragmentComparator;
	
	public AbstractCompositeFactory(IIndex index) {
		this.index= index;
		this.fragmentComparator= new FragmentBindingComparator( 
			new IIndexFragmentBindingComparator[] {
					new PDOMFragmentBindingComparator(), 
					new DefaultFragmentBindingComparator()
			}
		);
	}
	
	protected final IType[] getCompositeTypes(IType[] types) {
		// Don't create a new array until it's really needed.
		IType[] result = types;
		for (int i = 0; i < types.length; i++) {
			IType type = getCompositeType(types[i]);
			if (result != types) {
				result[i]= type;
			} else if (type != types[i]) {
				result = new IType[types.length];
				if (i > 0) {
					System.arraycopy(types, 0, result, 0, i);
				}
				result[i]= type;
			}
		}
		return result;
	}

	/**
	 * @see ICompositesFactory#getCompositeBindings(IIndexFragmentBinding[][])
	 */
	@Override
	public final IIndexBinding[] getCompositeBindings(IIndexFragmentBinding[][] fragmentBindings) {
		return getCompositeBindings(mergeBindingArrays(fragmentBindings));
	}

	private final IIndexBinding[] getCompositeBindings(IIndexFragmentBinding[] bindings) {
		IIndexBinding[] result = new IIndexBinding[bindings.length];
		for (int i = 0; i < result.length; i++)
			result[i] = getCompositeBinding(bindings[i]);
		return result;
	}

	@Override
	public final IIndexFragmentBinding[] findEquivalentBindings(IBinding binding) {
		CIndex cindex= (CIndex) index;
		try {
			return cindex.findEquivalentBindings(binding);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
		}
	}
 
	/**
	 * Convenience method for taking a group of binding arrays, and returning a single array
	 * with the each binding appearing once
	 * @param fragmentBindings
	 * @return an array of unique bindings
	 */
	protected IIndexFragmentBinding[] mergeBindingArrays(IIndexFragmentBinding[][] fragmentBindings) {
		TreeSet<IIndexFragmentBinding> ts = new TreeSet<IIndexFragmentBinding>(fragmentComparator);
		for (IIndexFragmentBinding[] array : fragmentBindings) {
			if (array != null) {
				for (IIndexFragmentBinding element : array) {
					ts.add(element);
				}
			}
		}
		return ts.toArray(new IIndexFragmentBinding[ts.size()]);
	}
	
	/**
	 * Convenience method for finding a binding with a definition (in the specified index
	 * context) which is equivalent to the specified binding. If no definition is found,
     * a declaration is returned if <code>allowDeclaration</code> is set, otherwise an
     * arbitrary binding is returned if available.
	 * @param binding the binding to find a representative for
	 * @param allowDeclaration whether declarations should be considered when a definition is
	 * unavailable
	 * @return the representative binding as defined above
	 */
	protected IIndexFragmentBinding findOneBinding(IBinding binding, boolean allowDeclaration) {
		try {
			IIndexFragmentBinding[] ibs= findEquivalentBindings(binding);
			IIndexFragmentBinding def= null;
			IIndexFragmentBinding dec= ibs.length > 0 ? ibs[0] : null;
			for (IIndexFragmentBinding ib : ibs) {
				if (ib.hasDefinition()) {
					def= ib;
				} else if (allowDeclaration && ib.hasDeclaration()) {
					dec= ib;
				}
			}
			return def == null ? dec : def;
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		throw new CompositingNotImplementedError();
	}
	
	private static class FragmentBindingComparator implements Comparator<IIndexFragmentBinding> {
		private IIndexFragmentBindingComparator[] comparators;
		
		FragmentBindingComparator(IIndexFragmentBindingComparator[] comparators) {
			this.comparators= comparators;
		}
		
		@Override
		public int compare(IIndexFragmentBinding f1, IIndexFragmentBinding f2) {
			for (IIndexFragmentBindingComparator comparator : comparators) {
				int cmp= comparator.compare(f1, f2);
				if (cmp != Integer.MIN_VALUE) {
					return cmp;
				}
			}
			throw new IllegalArgumentException();
		}
	}
}
