/*******************************************************************************
 * Copyright (c) 2010, 2015 Texas Instruments, Freescale Semiconductor and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Texas Instruments, Freescale Semiconductor - initial API and implementation
 *     Alvaro Sanchez-Leon (Ericsson AB) - [Memory] Support 16 bit addressable size (Bug 426730)
 *     Anders Dahlberg (Ericsson)  - Need additional API to extend support for memory spaces (Bug 431627)
 *     Alvaro Sanchez-Leon (Ericsson AB) - Need additional API to extend support for memory spaces (Bug 431627)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.memory;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.model.provisional.IMemorySpaceAwareMemoryBlock;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.model.DsfMemoryBlock;
import org.eclipse.cdt.dsf.debug.model.DsfMemoryBlockRetrieval;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IMemorySpaces.IMemorySpaceDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBMemory;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;

/**
 * A specialization of the DSF memory block implementation supporting memory
 * spaces. The memory space support is provisional, thus this class is internal.
 *
 * @author Alain Lee and John Cortell
 */
public class GdbMemoryBlock extends DsfMemoryBlock implements IMemorySpaceAwareMemoryBlock {

	private final String fMemorySpaceID;

	/**
	 * Constructor
	 */
	public GdbMemoryBlock(DsfMemoryBlockRetrieval retrieval, IMemoryDMContext context, String modelId,
			String expression, BigInteger address, int word_size, long length, String memorySpaceID) {
		super(retrieval, context, modelId, expression, address, word_size, length);
		fMemorySpaceID = (memorySpaceID != null && !memorySpaceID.isEmpty()) ? memorySpaceID : null;
		assert memorySpaceID == null || !memorySpaceID.isEmpty(); // callers shouldn't be passing in an empty string

		//TODO: remove the memorySpaceID parameter from this method
		//after making sure it's not used in earlier implementations
		//in the mean time check for consistency
		if (memorySpaceID != null) {
			assert (context instanceof IMemorySpaceDMContext);
			assert memorySpaceID.equals(((IMemorySpaceDMContext) context).getMemorySpaceId());
		} else {
			if (context instanceof IMemorySpaceDMContext) {
				assert ((IMemorySpaceDMContext) context).getMemorySpaceId() == null;
			}
		}
	}

	/**
	 * A memory space qualified context for the IMemory methods. Used if
	 * required, otherwise the more basic IMemoryDMContext is used
	 */
	public static class MemorySpaceDMContext extends AbstractDMContext implements IMemorySpaceDMContext {

		private final String fMemorySpaceId;

		public MemorySpaceDMContext(String sessionId, String memorySpaceId, IDMContext parent) {
			super(sessionId, new IDMContext[] { parent });
			// A memorySpaceDMContext should not be created if the memorySpaceId is not valid.
			// However we need the id to calculate the hash, therefore we can not leave it as null
			assert (memorySpaceId != null);
			fMemorySpaceId = memorySpaceId == null ? "" : memorySpaceId; //$NON-NLS-1$
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.debug.service.IMemorySpaces.IMemorySpaceDMContext#getMemorySpaceId()
		 */
		@Override
		public String getMemorySpaceId() {
			return fMemorySpaceId;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.datamodel.AbstractDMContext#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object other) {
			if (other instanceof MemorySpaceDMContext) {
				MemorySpaceDMContext dmc = (MemorySpaceDMContext) other;
				return (super.baseEquals(other)) && (dmc.fMemorySpaceId.equals(fMemorySpaceId));
			} else {
				return false;
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.datamodel.AbstractDMContext#hashCode()
		 */
		@Override
		public int hashCode() {
			return super.baseHashCode() + fMemorySpaceId.hashCode();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return baseToString() + ".memoryspace[" + fMemorySpaceId + ']'; //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.provisional.IMemorySpaceAwareMemoryBlock#getMemorySpaceID()
	 */
	@Override
	public String getMemorySpaceID() {
		return fMemorySpaceID;
	}

	/**
	 * Override this method to qualify the expression with the memory space, if
	 * applicable.
	 *
	 * @see org.eclipse.cdt.dsf.debug.model.DsfMemoryBlock#getExpression()
	 */
	@Override
	public String getExpression() {
		if (fMemorySpaceID != null) {
			assert !fMemorySpaceID.isEmpty();
			GdbMemoryBlockRetrieval retrieval = (GdbMemoryBlockRetrieval) getMemoryBlockRetrieval();
			return retrieval.encodeAddress(super.getExpression(), fMemorySpaceID);
		}
		return super.getExpression();
	}

	@Override
	public int getAddressSize() throws DebugException {
		GdbMemoryBlockRetrieval retrieval = (GdbMemoryBlockRetrieval) getMemoryBlockRetrieval();

		IGDBMemory memoryService = (IGDBMemory) retrieval.getServiceTracker().getService();
		if (memoryService != null) {
			return memoryService.getAddressSize(getContext());
		}

		throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED,
				Messages.Err_MemoryServiceNotAvailable, null));
	}

	@Override
	@DsfServiceEventHandler
	public void eventDispatched(ISuspendedDMEvent e) {
		super.eventDispatched(e);
		if (e.getReason() == StateChangeReason.BREAKPOINT || e.getReason() == StateChangeReason.EVENT_BREAKPOINT
				|| e.getReason() == StateChangeReason.WATCHPOINT) {
			// If the session is suspended because of a breakpoint we need to
			// fire DebugEvent.SUSPEND to force update for the "On Breakpoint" update mode.
			// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=406999.
			DebugEvent debugEvent = new DebugEvent(this, DebugEvent.SUSPEND, DebugEvent.BREAKPOINT);
			DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] { debugEvent });
		}
	}
}
