/*******************************************************************************
 * Copyright (c) 2010, 2015 Texas Instruments, Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Texas Instruments, Freescale Semiconductor - initial API and implementation
 *     Alvaro Sanchez-Leon (Ericsson AB) - [Memory] Support 16 bit addressable size (Bug 426730)
 *     Anders Dahlberg (Ericsson)  - Need additional API to extend support for memory spaces (Bug 431627)
 *     Alvaro Sanchez-Leon (Ericsson AB) - Need additional API to extend support for memory spaces (Bug 431627)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.memory;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.debug.core.model.provisional.IMemorySpaceAwareMemoryBlock;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.model.DsfMemoryBlock;
import org.eclipse.cdt.dsf.debug.model.DsfMemoryBlockRetrieval;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IMemorySpaces.IMemorySpaceDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBMemory;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.MemoryByte;

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
	public GdbMemoryBlock(DsfMemoryBlockRetrieval retrieval, IMemoryDMContext context,
			String modelId, String expression, BigInteger address,
			int word_size, long length, String memorySpaceID) {
		super(retrieval, context, modelId, expression, address, word_size, length);
		fMemorySpaceID = (memorySpaceID != null && !memorySpaceID.isEmpty()) ? memorySpaceID : null;
		assert memorySpaceID == null || !memorySpaceID.isEmpty();	// callers shouldn't be passing in an empty string
		
		//TODO: remove the memorySpaceID parameter from this method 
		//after making sure it's not used in earlier implementations
		//in the mean time check for consistency
		if(memorySpaceID != null) {
			assert(context instanceof IMemorySpaceDMContext);
			assert memorySpaceID.equals(((IMemorySpaceDMContext) context).getMemorySpaceId());
		} else {
			if (context instanceof IMemorySpaceDMContext) {
				assert ((IMemorySpaceDMContext) context).getMemorySpaceId() == null;
			}
		}
	}
	
    /*
	 * The real thing. Since the original call is synchronous (from a platform
	 * Job), we use a Query that will patiently wait for the underlying
	 * asynchronous calls to complete before returning.
	 * 
	 * @param bigAddress 
	 * @param count - The number of addressable units for this block
	 * @return MemoryByte[]
	 * @throws DebugException
	 */
    @Override
	protected MemoryByte[] fetchMemoryBlock(BigInteger bigAddress, final long count) throws DebugException {

    	// For the IAddress interface
    	final Addr64 address = new Addr64(bigAddress);
    	
        // Use a Query to synchronize the downstream calls  
        Query<MemoryByte[]> query = new Query<MemoryByte[]>() {
			@Override
			protected void execute(final DataRequestMonitor<MemoryByte[]> drm) {
				GdbMemoryBlockRetrieval retrieval = (GdbMemoryBlockRetrieval)getMemoryBlockRetrieval();
				int addressableSize = 1;
				try {
					addressableSize = getAddressableSize();
				} catch (DebugException e) {}

			    IMemory memoryService = retrieval.getServiceTracker().getService();
			    if (memoryService != null) {
			        // Go for it
			        memoryService.getMemory( 
			        		getContext(), address, 0, addressableSize, (int) count,
			            new DataRequestMonitor<MemoryByte[]>(retrieval.getExecutor(), drm) {
			                @Override
			                protected void handleSuccess() {
			                    drm.setData(getData());
			                    drm.done();
			                }
			            });
			    }
				else {
					drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, Messages.Err_MemoryServiceNotAvailable, null));
			    	drm.done();
			    	return;
			    }
			}
        };
		GdbMemoryBlockRetrieval retrieval = (GdbMemoryBlockRetrieval)getMemoryBlockRetrieval();        
        retrieval.getExecutor().execute(query);

		try {
            return query.get();
        } catch (InterruptedException e) {
    		throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR, Messages.Err_MemoryReadFailed, e));
        } catch (ExecutionException e) {
    		throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR, Messages.Err_MemoryReadFailed, e));
        }
    }

	/* Writes an array of bytes to memory.
     * 
     * @param offset
     * @param bytes
     * @throws DebugException
     */
    @Override
	protected void writeMemoryBlock(final long offset, final byte[] bytes) throws DebugException {

    	// For the IAddress interface
    	final Addr64 address = new Addr64(getBigBaseAddress());

        // Use a Query to synchronize the downstream calls  
        Query<MemoryByte[]> query = new Query<MemoryByte[]>() {
			@Override
			protected void execute(final DataRequestMonitor<MemoryByte[]> drm) {
				GdbMemoryBlockRetrieval retrieval = (GdbMemoryBlockRetrieval)getMemoryBlockRetrieval();
				int addressableSize = 1;
				try {
					addressableSize = getAddressableSize();
				} catch (DebugException e) {}
				
				int addressableUnits = bytes.length/addressableSize;

			    IMemory memoryService = retrieval.getServiceTracker().getService();
			    if (memoryService != null) {
			        // Go for it
	    	        memoryService.setMemory(
		    	  	      getContext(), address, offset, addressableSize, addressableUnits, bytes,
		    	  	      new RequestMonitor(retrieval.getExecutor(), drm));
			    }
			    else {
					drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, Messages.Err_MemoryServiceNotAvailable, null));			    	
			    	drm.done();
			    	return;
			    }
			}
        };
        GdbMemoryBlockRetrieval retrieval = (GdbMemoryBlockRetrieval)getMemoryBlockRetrieval();
        retrieval.getExecutor().execute(query);

		try {
            query.get();
        } catch (InterruptedException e) {
    		throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR, Messages.Err_MemoryWriteFailed, e));
        } catch (ExecutionException e) {
    		throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR, Messages.Err_MemoryWriteFailed, e));
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
			GdbMemoryBlockRetrieval retrieval = (GdbMemoryBlockRetrieval)getMemoryBlockRetrieval();
			return retrieval.encodeAddress(super.getExpression(), fMemorySpaceID);
		}
		return super.getExpression();
	}

	@Override
	public int getAddressSize() throws DebugException {
		GdbMemoryBlockRetrieval retrieval = (GdbMemoryBlockRetrieval)getMemoryBlockRetrieval();

		IMemory memoryService = retrieval.getServiceTracker().getService();
		if (memoryService instanceof IGDBMemory) {
			return ((IGDBMemory)memoryService).getAddressSize(getContext());
		}
		
		throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, Messages.Err_MemoryServiceNotAvailable, null));
	}
	
	@Override
	@DsfServiceEventHandler
	public void eventDispatched(ISuspendedDMEvent e) {
		super.eventDispatched(e);
		if (e.getReason() == StateChangeReason.BREAKPOINT ||
			e.getReason() == StateChangeReason.EVENT_BREAKPOINT ||
			e.getReason() == StateChangeReason.WATCHPOINT) {
			// If the session is suspended because of a breakpoint we need to 
			// fire DebugEvent.SUSPEND to force update for the "On Breakpoint" update mode.
			// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=406999. 
			DebugEvent debugEvent = new DebugEvent(this, DebugEvent.SUSPEND, DebugEvent.BREAKPOINT);
			DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] { debugEvent });
		}
	}
}
