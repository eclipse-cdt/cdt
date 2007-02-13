/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite;

import java.util.TreeSet;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.index.CIndex;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.core.runtime.CoreException;

/**
 * Commonality between composite factories
 */
public abstract class AbstractCompositeFactory implements ICompositesFactory {	
	protected IIndex index;
	
	public AbstractCompositeFactory(IIndex index) {
		this.index = index;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.index.composite.ICompositesFactory#getCompositeBindings(org.eclipse.cdt.core.index.IIndex, org.eclipse.cdt.internal.core.index.IIndexFragmentBinding[])
	 */
	public final IIndexBinding[] getCompositeBindings(IBinding[] bindings) {
		IIndexBinding[] result = new IIndexBinding[bindings.length];
		for(int i=0; i<result.length; i++)
			result[i] = getCompositeBinding(bindings[i]);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.index.composite.cpp.ICompositesFactory#getComposites(org.eclipse.cdt.core.index.IIndex, org.eclipse.cdt.internal.core.index.IIndexFragmentBinding[][])
	 */
	public final IIndexBinding[] getCompositeBindings(IIndexFragmentBinding[][] fragmentBindings) {
		return getCompositeBindings(mergeBindingArrays(fragmentBindings));
	}
 
	/**
	 * Convenience method for taking a group of binding arrays, and returning a single array
	 * with the each binding appearing once
	 * @param fragmentBindings
	 * @return an array of unique bindings
	 */
	protected static IIndexFragmentBinding[] mergeBindingArrays(IIndexFragmentBinding[][] fragmentBindings) {
		TreeSet ts = new TreeSet();
		for(int i=0; i<fragmentBindings.length; i++)
			for(int j=0; j<fragmentBindings[i].length; j++)
				ts.add(fragmentBindings[i][j]);
		return (IIndexFragmentBinding[]) ts.toArray(new IIndexFragmentBinding[ts.size()]);
	}

	/**
	 * Convenience method for finding a binding with a definition in the specified index
	 * context, which is equivalent to the specified binding
	 * @param index
	 * @param binding
	 * @return
	 */
	protected IIndexFragmentBinding findOneDefinition(IBinding binding) {
		try{
			CIndex cindex = (CIndex) index;
			IIndexFragmentBinding[] ibs = cindex.findEquivalentBindings(binding);
			IBinding def = ibs[0];
			for(int i=0; i<ibs.length; i++) {
				if(ibs[i].hasDefinition()) {
					def = ibs[i];
				}
			}
			return (IIndexFragmentBinding) def;
		} catch(CoreException ce) {
			CCorePlugin.log(ce);
		}
		throw new CompositingNotImplementedError();
	}
}
