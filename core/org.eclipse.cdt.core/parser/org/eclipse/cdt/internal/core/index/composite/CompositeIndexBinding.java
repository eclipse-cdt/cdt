/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Markus Schorn (Wind River Systems)
 *    Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents a binding which is split over several index fragments.
 * <p>
 * Like the component IIndexBinding objects that make up a CompositeIndexBinding, the 
 * it is only valid to call methods and obtain information while a read-lock is held on the
 * associated IIndex
 */
public abstract class CompositeIndexBinding implements IIndexBinding {
	/**
	 * The factory used for obtaining further composite bindings
	 */
	protected final ICompositesFactory cf;
	/**
	 * The representative binding for this composite binding. Most cases are simple
	 * enough that this becomes a delegate, some need to use it as a search key over fragments,
	 * and some ignore it as a representative binding from each fragment is needed to meet interface
	 * contracts.
	 */
	protected final IIndexFragmentBinding rbinding;
	
	public CompositeIndexBinding(ICompositesFactory cf, IIndexFragmentBinding rbinding) {
		if(rbinding == null || cf == null)
			throw new IllegalArgumentException();
		this.cf = cf;
		this.rbinding = rbinding;
	}
	
	public ILinkage getLinkage() throws CoreException {
		return rbinding.getLinkage();
	}

	public String getName() {
		return rbinding.getName();
	}

	public char[] getNameCharArray() {
		return rbinding.getNameCharArray();
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter.isInstance(rbinding)) {
			return rbinding;
		}
		return null;
	}

	public String[] getQualifiedName() {
		return new String[] {getName()};
	}
	
	public IIndexScope getScope() {
		return cf.getCompositeScope(rbinding.getScope());
	}
	
	public boolean hasDefinition() throws CoreException {
		fail(); return false;
	}
	
	protected final void fail() {
		throw new CompositingNotImplementedError("Compositing feature not implemented"); //$NON-NLS-1$
	}
	
	@Override
	public String toString() {
		return rbinding.toString();
	}
	
	public boolean isFileLocal() throws CoreException {
		return rbinding != null ? rbinding.isFileLocal() : false;
	}

	public IIndexFile getLocalToFile() throws CoreException {
		return rbinding != null ? rbinding.getLocalToFile() : null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof IIndexFragmentBinding)
			return rbinding.equals(obj);
		if (obj instanceof CompositeIndexBinding)
			return rbinding.equals(((CompositeIndexBinding)obj).rbinding);
		
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return rbinding.hashCode();
	}
	
	public IIndexBinding getOwner() {
		final IIndexFragmentBinding owner= rbinding.getOwner();
		if (owner == null)
			return null;
		
		return cf.getCompositeBinding(owner);
	}
	
	public IIndexBinding getRawBinding() {
		return rbinding;
	}
}
