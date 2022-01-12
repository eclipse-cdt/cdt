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

import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;

/**
 * @see ICallHierarchyProvider#findCalls
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 *
 * @since 5.8
 */
public interface ICallToResult {
	/**
	 * Adds a call point to the list of call hierarchy results.
	 *
	 * @param elems the called methods
	 * @param ref the call site
	 */
	public void add(ICElement[] elems, IIndexName ref);
}