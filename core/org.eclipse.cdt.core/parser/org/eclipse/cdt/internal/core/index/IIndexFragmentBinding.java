/*******************************************************************************
 * Copyright (c) 2006, 2013 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.core.runtime.CoreException;

public interface IIndexFragmentBinding extends IIndexBinding {
	IIndexFragmentBinding[] EMPTY_INDEX_BINDING_ARRAY= {};

	/**
	 * Returns the owner of the binding.
	 */
	IIndexFragment getFragment();

	/**
	 * Returns the linkage the binding belongs to.
	 */
	@Override
	ILinkage getLinkage();

	/**
     * Returns whether this binding has any definitions associated with it
     * in its associated fragment.
     */
	boolean hasDefinition() throws CoreException;

	/**
     * Returns whether this binding has a declaration or definition associated with
     * it in its associated fragment.
     */
	boolean hasDeclaration() throws CoreException;

	/**
	 * Returns the constant identifying the type of binding stored in the index
	 */
	int getBindingConstant();

	/**
	 * Returns the scope that contains this binding, or {@code null} for bindings in global scope.
	 */
	@Override
	IIndexScope getScope();

	/**
	 * {@inheritDoc}
	 * @since 5.1
	 */
	@Override
	IIndexFragmentBinding getOwner();

	/**
	 * Returns a unique id for the binding within the fragment, or {@code null} for unknown
	 * bindings.
	 * @since 5.1
	 */
	long getBindingID();
}