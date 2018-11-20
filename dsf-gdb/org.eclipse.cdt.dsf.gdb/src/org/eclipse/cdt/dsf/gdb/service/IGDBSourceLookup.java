/*******************************************************************************
 * Copyright (c) 2015 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - initial API and implementation to Add support for gdb's "set substitute-path" (Bug 472765)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup;

/**
 * Extension to the {@link ISourceLookup} service that allows GDB backend to
 * handle the path mapping source container type -
 * {@link MappingSourceContainer} - on the GDB side using GDB's
 * "set substitute-path" mechanism.
 *
 * @since 5.0
 */
public interface IGDBSourceLookup extends ISourceLookup {

	/**
	 * Initialise the source substitutions on the GDB backend (aka do the
	 * initial "set substitute-path"s)
	 *
	 * @param sourceLookupCtx
	 * @param rm
	 */
	void initializeSourceSubstitutions(ISourceLookupDMContext sourceLookupCtx, RequestMonitor rm);

	/**
	 * Update the source substitutions on the GDB backend (aka modify the
	 * "set substitute-path"s)
	 *
	 * @param sourceLookupCtx
	 * @param rm
	 *            with the result set to True if a change was made
	 */
	void sourceContainersChanged(ISourceLookupDMContext sourceLookupCtx, DataRequestMonitor<Boolean> rm);
}
