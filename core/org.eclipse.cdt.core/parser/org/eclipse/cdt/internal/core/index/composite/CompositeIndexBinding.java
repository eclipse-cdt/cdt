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

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.index.IIndexBinding;
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
	protected final IBinding rbinding;
	
	public CompositeIndexBinding(ICompositesFactory cf, IBinding rbinding) {
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

	public Object getAdapter(Class adapter) {
		fail();
		return null;
	}

	public String[] getQualifiedName() {
		return new String[] {getName()};
	}
	
	public IScope getScope() throws DOMException {
		return cf.getCompositeScope(rbinding.getScope());
	}
	
	public boolean hasDefinition() throws CoreException {
		fail(); return false;
	}
	
	protected final void fail() {
		throw new CompositingNotImplementedError("Compositing feature not implemented"); //$NON-NLS-1$
	}
	
	public String toString() {
		return rbinding.toString();
	}
	
	public boolean isFileLocal() throws CoreException {
		return rbinding instanceof IIndexBinding ? ((IIndexBinding)rbinding).isFileLocal() : true;
	}
}
