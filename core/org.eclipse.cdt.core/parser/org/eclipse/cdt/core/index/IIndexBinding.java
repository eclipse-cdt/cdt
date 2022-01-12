/*******************************************************************************
 * Copyright (c) 2006, 2013 Wind River Systems, Inc. and others.
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
 *******************************************************************************/
package org.eclipse.cdt.core.index;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents the semantics of a name in the index.
 * @since 4.0
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IIndexBinding extends IBinding {
	IIndexBinding[] EMPTY_INDEX_BINDING_ARRAY = {};

	/**
	 * Returns the qualified name of this binding as array of strings.
	 */
	String[] getQualifiedName();

	/**
	 * Returns whether the scope of the binding is file-local. A file local
	 * binding is private to an index and should not be adapted to a binding
	 * in another index.
	 */
	boolean isFileLocal() throws CoreException;

	/**
	 * Returns the file this binding is local to, or <code>null</code> for global
	 * bindings.
	 * A binding is local if a file has a separate instances of the binding. This
	 * is used to model static files, static variables.
	 */
	IIndexFile getLocalToFile() throws CoreException;

	/**
	 * @since 5.1
	 */
	@Override
	IIndexBinding getOwner();
}
