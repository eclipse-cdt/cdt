package org.eclipse.dd.gdb.internal.provisional.service;

import java.util.Hashtable;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.IMemory;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.mi.service.IMIContainerDMContext;
import org.eclipse.dd.mi.service.IMIExecutionDMContext;
import org.eclipse.dd.mi.service.MIMemory;
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

		setMemoryCache(new GDBMemoryCache());

		requestMonitor.done();
	}

	@Override
	public void shutdown(final RequestMonitor requestMonitor) {
		unregister();
		super.shutdown(requestMonitor);
	}

	protected class GDBMemoryCache extends MIMemoryCache {
		@Override
		protected void readMemoryBlock(IDMContext dmc, IAddress address, final long offset,
				final int word_size, final int count, final DataRequestMonitor<MemoryByte[]> drm)
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
					}
				}
			}
			
			super.readMemoryBlock(threadOrMemoryDmc, address, offset, word_size, count, drm);
		}

		/**
		 * @param memoryDMC
		 * @param address
		 * @param offset
		 * @param word_size
		 * @param count
		 * @param buffer
		 * @param rm
		 */
		@Override
		protected void writeMemoryBlock(final IDMContext dmc, final IAddress address, final long offset,
				final int word_size, final int count, final byte[] buffer, final RequestMonitor rm)
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
					}
				}
			}

			super.writeMemoryBlock(threadOrMemoryDmc, address, offset, word_size, count, buffer, rm);
		}
	}

}
