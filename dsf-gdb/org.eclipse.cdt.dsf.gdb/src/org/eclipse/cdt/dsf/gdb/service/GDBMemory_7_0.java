/*******************************************************************************
 * Copyright (c) 2008, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Hashtable;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IMemorySpaces.IMemorySpaceDMContext;
import org.eclipse.cdt.dsf.gdb.internal.memory.GdbMemoryBlock.MemorySpaceDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.MIMemory;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.model.MemoryByte;

public class GDBMemory_7_0 extends MIMemory {

	public GDBMemory_7_0(DsfSession session) {
		super(session);
	}

	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(
				new RequestMonitor(getExecutor(), requestMonitor) { 
					@Override
					public void handleSuccess() {
						doInitialize(requestMonitor);
					}});
	}

	private void doInitialize(final RequestMonitor requestMonitor) {
		register(new String[] { MIMemory.class.getName(), IMemory.class.getName(), GDBMemory_7_0.class.getName()}, 
				 new Hashtable<String, String>());

		requestMonitor.done();
	}

	@Override
	public void shutdown(final RequestMonitor requestMonitor) {
		unregister();
		super.shutdown(requestMonitor);
	}

	@Override
	protected void readMemoryBlock(IDMContext dmc, IAddress address, long offset,
			int word_size, int count, DataRequestMonitor<MemoryByte[]> drm)
	{
		IDMContext threadOrMemoryDmc = dmc;

		IMIContainerDMContext containerCtx = DMContexts.getAncestorOfType(dmc, IMIContainerDMContext.class);
		if(containerCtx != null) {
			IGDBProcesses procService = getServicesTracker().getService(IGDBProcesses.class);

			if (procService != null) {
				IMIExecutionDMContext[] execCtxs = procService.getExecutionContexts(containerCtx);
				// Return any thread... let's take the first one.
				if (execCtxs != null && execCtxs.length > 0) {
					threadOrMemoryDmc = execCtxs[0];

					// Not so fast, Charlie. The context we were given may have
					// a memory space qualifier. We need to preserve it.
					if (dmc instanceof IMemorySpaceDMContext) {
						threadOrMemoryDmc = new MemorySpaceDMContext(getSession().getId(), ((IMemorySpaceDMContext)dmc).getMemorySpaceId(), threadOrMemoryDmc);
					}
				}
			}
		}

		super.readMemoryBlock(threadOrMemoryDmc, address, offset, word_size, count, drm);
	}

	@Override
	protected void writeMemoryBlock(IDMContext dmc, IAddress address, long offset,
			int word_size, int count, byte[] buffer, RequestMonitor rm)
	{
		IDMContext threadOrMemoryDmc = dmc;

		IMIContainerDMContext containerCtx = DMContexts.getAncestorOfType(dmc, IMIContainerDMContext.class);
		if(containerCtx != null) {
			IGDBProcesses procService = getServicesTracker().getService(IGDBProcesses.class);

			if (procService != null) {
				IMIExecutionDMContext[] execCtxs = procService.getExecutionContexts(containerCtx);
				// Return any thread... let's take the first one.
				if (execCtxs != null && execCtxs.length > 0) {
					threadOrMemoryDmc = execCtxs[0];
					
					// Not so fast, Charlie. The context we were given may have
					// a memory space qualifier. We need to preserve it.
					if (dmc instanceof IMemorySpaceDMContext) {
						threadOrMemoryDmc = new MemorySpaceDMContext(getSession().getId(), ((IMemorySpaceDMContext)dmc).getMemorySpaceId(), threadOrMemoryDmc);
					}
				}
			}
		}

		super.writeMemoryBlock(threadOrMemoryDmc, address, offset, word_size, count, buffer, rm);
	}
}
