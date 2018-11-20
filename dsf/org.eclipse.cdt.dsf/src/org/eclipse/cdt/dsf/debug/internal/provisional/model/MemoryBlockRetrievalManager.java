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
package org.eclipse.cdt.dsf.debug.internal.provisional.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.model.DsfMemoryBlockRetrieval;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IMemoryBlockManager;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;

/* (non-Javadoc)
 * @see org.eclipse.cdt.dsf.debug.internal.provisional.model.IMemoryBlockRetrievalManager
 */
public class MemoryBlockRetrievalManager implements IMemoryBlockRetrievalManager {

	private final String fModelId;
	private final DsfSession fSession;
	private final ILaunchConfiguration fLaunchConfig;
	private final Map<IMemoryDMContext, IMemoryBlockRetrieval> fMapMemDMCToBlockRetrieval = new HashMap<>();

	/**
	 * Constructor
	 */
	public MemoryBlockRetrievalManager(String modelId, ILaunchConfiguration config, DsfSession session) {
		fModelId = modelId;
		fSession = session;
		fLaunchConfig = config;
	}

	@DsfServiceEventHandler
	public void eventDispatched(IStartedDMEvent event) {
		// If a new memory context is starting, create its memory retrieval instance
		if (event.getDMContext() instanceof IMemoryDMContext) {
			IMemoryDMContext memDmc = (IMemoryDMContext) event.getDMContext();
			if (!fMapMemDMCToBlockRetrieval.containsKey(memDmc)) {
				// We need a new memory retrieval for this new memory context
				IMemoryBlockRetrieval memRetrieval = null;
				try {
					memRetrieval = createMemoryBlockRetrieval(fModelId, fLaunchConfig, fSession);
				} catch (DebugException e) {
					DsfPlugin.getDefault().getLog().log(e.getStatus());
					return;
				}

				if (memRetrieval != null) {
					if (memRetrieval instanceof DsfMemoryBlockRetrieval) {
						((DsfMemoryBlockRetrieval) memRetrieval).initialize(memDmc);
					}

					fMapMemDMCToBlockRetrieval.put(memDmc, memRetrieval);
				}
			}
		}
	}

	@DsfServiceEventHandler
	public void eventDispatched(IExitedDMEvent event) {
		// If a memory context is exiting, save expressions and clean its used resources
		if (event.getDMContext() instanceof IMemoryDMContext) {
			IMemoryDMContext memDmc = (IMemoryDMContext) event.getDMContext();
			// Remove entry if it exists
			final IMemoryBlockRetrieval retrieval = fMapMemDMCToBlockRetrieval.remove(memDmc);
			if (retrieval != null) {
				if (retrieval instanceof DsfMemoryBlockRetrieval) {
					((DsfMemoryBlockRetrieval) retrieval).saveMemoryBlocks();
				}
				// Fire a terminate event for the memory retrieval object so
				// that the hosting memory views can clean up. See 255120 and
				// 283586
				DebugPlugin.getDefault()
						.fireDebugEventSet(new DebugEvent[] { new DebugEvent(retrieval, DebugEvent.TERMINATE) });

				Job removeJob = new Job("Removing memory blocks") { //$NON-NLS-1$

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						IMemoryBlockManager mbm = DebugPlugin.getDefault().getMemoryBlockManager();
						IMemoryBlock[] deletedMemoryBlocks = mbm.getMemoryBlocks(retrieval);
						mbm.removeMemoryBlocks(deletedMemoryBlocks);
						return Status.OK_STATUS;
					}
				};
				removeJob.schedule();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.internal.provisional.model.IMemoryBlockRetrievalManager#getMemoryBlockRetrieval(org.eclipse.cdt.dsf.datamodel.IDMContext)
	 */
	@Override
	public IMemoryBlockRetrieval getMemoryBlockRetrieval(IDMContext dmc) {
		IMemoryBlockRetrieval memRetrieval = null;
		IMemoryDMContext memDmc = DMContexts.getAncestorOfType(dmc, IMemoryDMContext.class);
		if (memDmc != null) {
			memRetrieval = fMapMemDMCToBlockRetrieval.get(memDmc);
		}

		return memRetrieval;
	}

	protected IMemoryBlockRetrieval createMemoryBlockRetrieval(String model, ILaunchConfiguration config,
			DsfSession session) throws DebugException {
		return new DsfMemoryBlockRetrieval(model, config, session);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.internal.provisional.model.IMemoryBlockRetrievalManager#dispose()
	 */
	@Override
	public void dispose() {
		// release resources / references
		fMapMemDMCToBlockRetrieval.clear();
	}
}
