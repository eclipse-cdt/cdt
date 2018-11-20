/*******************************************************************************
 * Copyright (c) 2007, 2015 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Markus Schorn (Wind River Systems)
 *    Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
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
		if (rbinding == null || cf == null)
			throw new IllegalArgumentException();
		this.cf = cf;
		this.rbinding = rbinding;
	}

	@Override
	public ILinkage getLinkage() {
		return rbinding.getLinkage();
	}

	@Override
	public String getName() {
		return rbinding.getName();
	}

	@Override
	public char[] getNameCharArray() {
		return rbinding.getNameCharArray();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.isInstance(rbinding)) {
			return (T) rbinding;
		}
		return null;
	}

	@Override
	public String[] getQualifiedName() {
		return new String[] { getName() };
	}

	@Override
	public IIndexScope getScope() {
		return cf.getCompositeScope(rbinding.getScope());
	}

	public boolean hasDefinition() throws CoreException {
		fail();
		return false;
	}

	protected final void fail() {
		throw new CompositingNotImplementedError("Compositing feature not implemented"); //$NON-NLS-1$
	}

	@Override
	public String toString() {
		return rbinding.toString();
	}

	@Override
	public boolean isFileLocal() throws CoreException {
		return rbinding != null && rbinding.isFileLocal();
	}

	@Override
	public IIndexFile getLocalToFile() throws CoreException {
		return rbinding != null ? rbinding.getLocalToFile() : null;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (!(other instanceof CompositeIndexBinding))
			return false;
		CompositeIndexBinding otherComposite = (CompositeIndexBinding) other;
		return rbinding.equals(otherComposite.rbinding) && cf.equals(otherComposite.cf);
	}

	@Override
	public int hashCode() {
		return rbinding.hashCode();
	}

	@Override
	public IIndexBinding getOwner() {
		final IIndexFragmentBinding owner = rbinding.getOwner();
		if (owner == null)
			return null;

		return cf.getCompositeBinding(owner);
	}

	public IIndexBinding getRawBinding() {
		return rbinding;
	}

	protected IIndexFragmentBinding adaptBinding(IBinding binding) {
		if (binding instanceof IIndexFragmentBinding) {
			return (IIndexFragmentBinding) binding;
		}
		ILinkage linkage = rbinding.getLinkage();
		if (linkage instanceof PDOMLinkage) {
			try {
				return ((PDOMLinkage) linkage).adaptBinding(binding);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return null;
	}
}
