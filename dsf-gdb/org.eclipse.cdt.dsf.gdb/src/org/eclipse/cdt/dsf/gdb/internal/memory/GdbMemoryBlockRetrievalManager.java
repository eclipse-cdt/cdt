/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson AB) - Each memory context needs a different MemoryRetrieval (Bug 250323)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.memory;

import org.eclipse.cdt.dsf.debug.internal.provisional.model.MemoryBlockRetrievalManager;
import org.eclipse.cdt.dsf.debug.model.DsfMemoryBlockRetrieval;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;

public class GdbMemoryBlockRetrievalManager extends MemoryBlockRetrievalManager {

	public GdbMemoryBlockRetrievalManager(String modelId, ILaunchConfiguration config, DsfSession session) {
		super(modelId, config, session);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.internal.provisional.model.MemoryBlockRetrievalManager#createMemoryBlockRetrieval(java.lang.String, org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.cdt.dsf.service.DsfSession)
	 */
	@Override
	protected IMemoryBlockRetrieval createMemoryBlockRetrieval(String model, ILaunchConfiguration config,
			DsfSession session) {
		DsfMemoryBlockRetrieval memRetrieval = null;

		try {
			memRetrieval = new GdbMemoryBlockRetrieval(model, config, session);
		} catch (DebugException e) {
			GdbPlugin.getDefault().getLog().log(e.getStatus());
		}

		return memRetrieval;
	}

}
