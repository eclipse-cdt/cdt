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

import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.runtime.CoreException;

/**
 * Interface for classes implementing the org.eclipse.cdt.ui.callHierarchyProviders extension
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
public interface ICallHierarchyProvider {
	/**
	 * Adds the calling functions and the corresponding call points located in files not covered
	 * by the CDT index to the call hierarchy results.
	 *
	 * @param callee the function being called
	 * @param linkageID the ID of the linkage to search for calling functions or -1 to search
	 *     all linkages.
	 * @param index the index, for which the caller is holding a read lock
	 * @param result the accumulator to add the results to
	 * @throws CoreException may be thrown in case of an error
	 */
	void findCalledBy(ICElement callee, int linkageID, IIndex index, ICalledByResult result) throws CoreException;

	/**
	 * Adds the called functions and the corresponding call points for a function defined in a file
	 * not covered by the CDT index to the call hierarchy results.
	 *
	 * @param caller the function to get the called functions for
	 * @param index the index, for which the caller is holding a read lock
	 * @param result the accumulator to add the results to
	 * @throws CoreException may be thrown in case of an error
	 */
	void findCalls(ICElement caller, IIndex index, ICallToResult result) throws CoreException;

	/**
	 * Checks if an element representing a function is owned by the call hierarchy provider.
	 *
	 * @param element the element to check
	 * @return {@code true} if the element is owned by this provider.
	 */
	boolean ownsElement(ICElement element);
}
