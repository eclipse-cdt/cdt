/*******************************************************************************
 * Copyright (c) 2010, Texas Instruments, Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Texas Instruments, Freescale Semiconductor - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.memory;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.debug.core.model.provisional.IMemorySpaceAwareMemoryBlock;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.model.DsfMemoryBlock;
import org.eclipse.cdt.dsf.debug.model.DsfMemoryBlockRetrieval;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IMemorySpaces;
import org.eclipse.cdt.dsf.debug.service.IMemorySpaces.IMemorySpaceDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
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
	GdbMemoryBlock(DsfMemoryBlockRetrieval retrieval, IMemoryDMContext context,
			String modelId, String expression, BigInteger address,
			int word_size, long length, String memorySpaceID) {
		super(retrieval, context, modelId, expression, address, word_size, length);
		fMemorySpaceID = (memorySpaceID != null && memorySpaceID.length() > 0) ? memorySpaceID : null;
		assert memorySpaceID == null || memorySpaceID.length() > 0;	// callers shouldn't be passing in an empty string
	}

	/**
	 * A memory space qualified context for the IMemory methods. Used if
	 * required, otherwise the more basic IMemoryDMContext is used
	 */
	public static class MemorySpaceDMContext extends AbstractDMContext implements IMemorySpaceDMContext {

		private final String fMemorySpaceId;

		public MemorySpaceDMContext(String sessionId, String memorySpaceId, IDMContext parent) {
			super(sessionId, new IDMContext[] {parent});
			fMemorySpaceId = memorySpaceId;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.debug.service.IMemorySpaces.IMemorySpaceDMContext#getMemorySpaceId()
		 */
		public String getMemorySpaceId() {
			return fMemorySpaceId;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.datamodel.AbstractDMContext#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object other) {
            if (other instanceof MemorySpaceDMContext) {
            	MemorySpaceDMContext  dmc = (MemorySpaceDMContext) other;
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
        	return baseToString() + ".memoryspace[" + fMemorySpaceId + ']';  //$NON-NLS-1$
        } 
	}
	
    /*
	 * The real thing. Since the original call is synchronous (from a platform
	 * Job), we use a Query that will patiently wait for the underlying
	 * asynchronous calls to complete before returning.
	 * 
	 * @param bigAddress 
	 * @param length 
	 * @return MemoryByte[] 
	 * @throws DebugException
	 */
    @Override
	protected MemoryByte[] fetchMemoryBlock(BigInteger bigAddress, final long length) throws DebugException {

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

				// If this block was created with a memory space qualification,
				// we need to create an enhanced context
				IMemoryDMContext context = null;
				if (fMemorySpaceID != null) {
				    IMemorySpaces memoryService = (IMemorySpaces) retrieval.getMemorySpaceServiceTracker().getService();
				    if (memoryService != null) {
						context = new MemorySpaceDMContext(memoryService.getSession().getId(), fMemorySpaceID, getContext());
				    }
					else {
						drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, Messages.Err_MemoryServiceNotAvailable, null));
				    	drm.done();
				    	return;
				    }
				}
				else {
					 context = getContext();
				}
						
			    IMemory memoryService = (IMemory) retrieval.getServiceTracker().getService();
			    if (memoryService != null) {
			        // Go for it
			        memoryService.getMemory( 
			        	context, address, 0, addressableSize, (int) length,
			            //getContext(), address, 0, addressableSize, (int) length,
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
				// If this block was created with a memory space qualification,
				// we need to create an enhanced context
				IMemoryDMContext context = null;
				if (fMemorySpaceID != null) {
				    IMemorySpaces memoryService = (IMemorySpaces) retrieval.getMemorySpaceServiceTracker().getService();
				    if (memoryService != null) {
						context = new MemorySpaceDMContext(memoryService.getSession().getId(), fMemorySpaceID, getContext());
				    }
					else {
						drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, Messages.Err_MemoryServiceNotAvailable, null));
				    	drm.done();
				    	return;
				    }
				}
				else {
					 context = getContext();
				}
			    IMemory memoryService = (IMemory) retrieval.getServiceTracker().getService();
			    if (memoryService != null) {
			        // Go for it
	    	        memoryService.setMemory(
		    	  	      context, address, offset, addressableSize, bytes.length, bytes,
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
			assert fMemorySpaceID.length() > 0;
			GdbMemoryBlockRetrieval retrieval = (GdbMemoryBlockRetrieval)getMemoryBlockRetrieval();
			return retrieval.encodeAddress(super.getExpression(), fMemorySpaceID);
		}
		return super.getExpression();
	}
}
