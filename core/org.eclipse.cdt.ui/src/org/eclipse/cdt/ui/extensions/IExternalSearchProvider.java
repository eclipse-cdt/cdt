/*******************************************************************************
 * Copyright (c) 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.extensions;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;

/**
 * Interface for classes implementing the org.eclipse.cdt.ui.externalSearchProviders extension
 * point.
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 *
 * @since 5.8
 */
public interface IExternalSearchProvider {
	/**
	 * Searches for all names that resolve to the given binding. The result can be limited to
	 * references, declarations or definitions, or a combination of those.
	 * <p>
	 * When the method is called, the {@code foundNames} list contains the names found in the core
	 * index. The method may add more names to the {@code foundNames} list, but should not remove
	 * the names already present there.
	 *
	 * @param binding A binding for which names are searched for
	 * @param flags A combination of {@link IIndex#FIND_DECLARATIONS},
	 *     {@link IIndex#FIND_DEFINITIONS}, {@link IIndex#FIND_REFERENCES} and
	 *     {@link IIndex#SEARCH_ACROSS_LANGUAGE_BOUNDARIES}.
	 * @param projects The projects in context of which to do search.
	 * @param foundNames Before the call contains the names found in the core index. After the call
	 *     may contain additional names located in files covered by the index extension.
	 * @throws CoreException may be thrown in case of an error
	 *
	 * @see {@link IIndex#findNames(IBinding, int)}
	 */
	void findNames(IBinding binding, int flags, ICProject[] projects, IIndex index, List<IIndexName> foundNames)
			throws CoreException;

	/**
	 * Finds the function or the class enclosing the given name.
	 *
	 * @param name The name to find the enclosing element for.
	 * @return The function or the class enclosing the given name, or {@code null} if there is
	 *     no enclosing element or the name is not associated with this search provider.
	 */
	ICElement getEnclosingElement(IIndexName name);
}
