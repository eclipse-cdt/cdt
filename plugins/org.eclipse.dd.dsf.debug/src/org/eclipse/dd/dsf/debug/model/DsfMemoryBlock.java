/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson Communication - upgrade IF to IMemoryBlockExtension
 *     Ericsson Communication - added support for 64 bit processors
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.Query;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.debug.service.IMemory;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IMemory.MemoryChangedEvent;
import org.eclipse.dd.dsf.service.DsfServiceEventHandler;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.MemoryByte;

/**
 * This class manages the memory block retrieved from the target as a result
 * of a getBytesFromAddress() call from the platform.
 * 
 * It performs its read/write functions using the MemoryService.
 */
public class DsfMemoryBlock extends PlatformObject implements IMemoryBlockExtension
{
    private final ILaunch fLaunch;
    private final IDebugTarget fDebugTarget;
    private final DsfMemoryBlockRetrieval fRetrieval;
    private final String fModelId;
    private final String fExpression;
    protected BigInteger fBaseAddress;
    protected long fLength;
    
    private ArrayList<Object> fConnections = new ArrayList<Object>();
    private boolean isEnabled;

    /**
     * Constructor.
     * 
     * @param retrieval    - the MemoryBlockRetrieval (session context)
     * @param modelId      - 
     * @param expression   - the displayed expression in the UI
     * @param address      - the actual memory block start address
     * @param length       - the memory block length (could be 0)
     */
    DsfMemoryBlock(DsfMemoryBlockRetrieval retrieval, String modelId, String expression, BigInteger address, long length) {

    	fLaunch      = null;		// TODO: fRetrieval.getLaunch();
    	fDebugTarget = null;		// TODO: fRetrieval.getDebugTarget();
        fRetrieval   = retrieval;
        fModelId     = modelId;
        fExpression  = expression;
        fBaseAddress = address;
        fLength      = length;
        
        try {
            fRetrieval.getExecutor().execute(new Runnable() {
                public void run() {
                    fRetrieval.getSession().addServiceEventListener(DsfMemoryBlock.this, null);
                }
            });
        } catch (RejectedExecutionException e) {
            // Session is shut down.
        }
    }

    // ////////////////////////////////////////////////////////////////////////
    // IAdaptable
    // ////////////////////////////////////////////////////////////////////////

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
	@Override
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(DsfMemoryBlockRetrieval.class)) {
            return fRetrieval;
        }
        return super.getAdapter(adapter);
    }

    // ////////////////////////////////////////////////////////////////////////
    // IDebugElement
    // ////////////////////////////////////////////////////////////////////////

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
     */
    public IDebugTarget getDebugTarget() {
        return fDebugTarget;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
     */
    public String getModelIdentifier() {
        return fModelId;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
     */
    public ILaunch getLaunch() {
        return fLaunch;
    }

    // ////////////////////////////////////////////////////////////////////////
    // IMemoryBock interface - obsoleted by IMemoryBlockExtension
    // ////////////////////////////////////////////////////////////////////////

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IMemoryBlock#getStartAddress()
     */
    public long getStartAddress() {
    	// Not implemented (obsolete)
    	return 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IMemoryBlock#getLength()
     */
    public long getLength() {
    	// Not implemented (obsolete)
        return 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IMemoryBlock#getBytes()
     */
    public byte[] getBytes() throws DebugException {
    	// Not implemented (obsolete)
    	return new byte[0];
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IMemoryBlock#supportsValueModification()
     */
    public boolean supportsValueModification() {
    	// return fDebugTarget.supportsValueModification(this);
    	return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IMemoryBlock#setValue(long, byte[])
     */
    public void setValue(long offset, byte[] bytes) throws DebugException {
    	// Not implemented (obsolete)
    }

    // ////////////////////////////////////////////////////////////////////////
    // IMemoryBlockExtension interface
    // ////////////////////////////////////////////////////////////////////////

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getExpression()
	 */
	public String getExpression() {
		return fExpression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getBigBaseAddress()
	 */
	public BigInteger getBigBaseAddress() throws DebugException {
        return fBaseAddress;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getMemoryBlockStartAddress()
	 */
	public BigInteger getMemoryBlockStartAddress() throws DebugException {
		// Null indicates that memory can be retrieved at addresses lower than the block base address
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getMemoryBlockEndAddress()
	 */
	public BigInteger getMemoryBlockEndAddress() throws DebugException {
		// Null indicates that memory can be retrieved at addresses higher the block base address
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getBigLength()
	 */
	public BigInteger getBigLength() throws DebugException {
		// -1 indicates that memory block is unbounded
		return BigInteger.valueOf(-1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getAddressSize()
	 */
	public int getAddressSize() throws DebugException {
//		// TODO:
//		try {
//			return fDebugTarget.getAddressSize();
//		} catch (CoreException e) {
//			throw new DebugException(e.getStatus());
//		}
		return 4;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#supportBaseAddressModification()
	 */
	public boolean supportBaseAddressModification() throws DebugException {
		// TODO: return fDebugTarget.supportBaseAddressModification(this);
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#supportsChangeManagement()
	 */
	public boolean supportsChangeManagement() {
		// Let the UI handle block content modification
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#setBaseAddress(java.math.BigInteger)
	 */
	public void setBaseAddress(BigInteger address) throws DebugException {
		fBaseAddress = address;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getBytesFromOffset(java.math.BigInteger, long)
	 */
	public MemoryByte[] getBytesFromOffset(BigInteger offset, long units) throws DebugException {
		return getBytesFromAddress(fBaseAddress.add(offset), units);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getBytesFromAddress(java.math.BigInteger, long)
	 */
	public MemoryByte[] getBytesFromAddress(BigInteger address, long units) throws DebugException {
		fLength = units;
		MemoryByte[] block = fetchMemoryBlock(address, units);
		return block;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#setValue(java.math.BigInteger, byte[])
	 */
	public void setValue(BigInteger offset, byte[] bytes) throws DebugException {
		writeMemoryBlock(offset.longValue(), bytes);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#connect(java.lang.Object)
	 */
	public void connect(Object client) {
		if (!fConnections.contains(client))
			fConnections.add(client);
		if (fConnections.size() == 1)
			enable();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#disconnect(java.lang.Object)
	 */
	public void disconnect(Object client) {
		if (fConnections.contains(client))
			fConnections.remove(client);
		if (fConnections.size() == 0)
			disable();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getConnections()
	 */
	public Object[] getConnections() {
		return fConnections.toArray();
	}

	private void enable() {
		isEnabled = true;
	}

	private void disable() {
		isEnabled = false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#dispose()
	 */
	public void dispose() throws DebugException {
		try {
    		fRetrieval.getExecutor().execute(new Runnable() {
    		    public void run() {
    		        fRetrieval.getSession().removeServiceEventListener(DsfMemoryBlock.this);
    		    }
    		});
		} catch (RejectedExecutionException e) {
		    // Session is shut down.
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getMemoryBlockRetrieval()
	 */
	public IMemoryBlockRetrieval getMemoryBlockRetrieval() {
		return fRetrieval;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getAddressableSize()
	 */
	public int getAddressableSize() throws DebugException {
		// TODO: return fDebugTarget.getAddressableSize();
		return 1;
	}

    // ////////////////////////////////////////////////////////////////////////
    // Helper functions
    // ////////////////////////////////////////////////////////////////////////

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
    private MemoryByte[] fetchMemoryBlock(BigInteger bigAddress, final long length) throws DebugException {

    	// For the IAddress interface
    	final Addr64 address = new Addr64(bigAddress);
    	final int word_size = 1;
    	
        // Use a Query to "synchronize" the inherently asynchronous downstream calls  
        Query<MemoryByte[]> query = new Query<MemoryByte[]>() {
			@Override
			protected void execute(final DataRequestMonitor<MemoryByte[]> rm) {
			    IMemory memoryService = (IMemory) fRetrieval.getServiceTracker().getService();
			    if (memoryService != null) {
			        // Go for it
			        memoryService.getMemory( 
			            fRetrieval.getContext(), address, 0, word_size, (int) length,
			            new DataRequestMonitor<MemoryByte[]>(fRetrieval.getExecutor(), rm) {
			                @Override
			                protected void handleOK() {
			                    rm.setData(getData());
			                    rm.done();
			                }
			            });
			        }
				
			    }
            };
        fRetrieval.getExecutor().execute(query);
        try {
            return query.get();
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        }

        return null;
    }

    @DsfServiceEventHandler
    public void eventDispatched(MemoryChangedEvent e) {
        handleMemoryChange(e.getAddress().getValue());
    }
    
    @DsfServiceEventHandler 
    public void eventDispatched(IRunControl.ISuspendedDMEvent e) {
        handleMemoryChange(BigInteger.ZERO);
    }
    
    /* Writes an array of bytes to memory.
     * 
     * @param offset
     * @param bytes
     * @throws DebugException
     */
    private void writeMemoryBlock(final long offset, final byte[] bytes) throws DebugException {

    	// For the IAddress interface
    	final Addr64 address = new Addr64(fBaseAddress);
    	final int word_size = 1;

    	final IMemory memoryService = (IMemory) fRetrieval.getServiceTracker().getService();
	    if (memoryService != null) {
	    	memoryService.getExecutor().execute(new Runnable() {
	    		public void run() {
	    	        memoryService.setMemory(
	    	  	          fRetrieval.getContext(), address, offset, word_size, bytes.length, bytes,
	    	  	          new RequestMonitor(fRetrieval.getExecutor(), null) {
	    	  	              @Override
	    	  	              protected void handleOK() {
	    	  	            	  // handleMemoryChange(fBaseAddress);
	    	  	              }
	    	  	          });
	    		}
	    	});
	    }
    }

    /**
	 * @param address
	 * @param length
	 */
	public void handleMemoryChange(BigInteger address) {
		// Check if the change affects this particular block (0 is universal)
		BigInteger fEndAddress = fBaseAddress.add(BigInteger.valueOf(fLength));
		if (address.equals(BigInteger.ZERO) ||
		   ((fBaseAddress.compareTo(address) != 1) && (fEndAddress.compareTo(address) == 1)))
		{
			// Notify the event listeners
			DebugEvent debugEvent = new DebugEvent(this, DebugEvent.CHANGE, DebugEvent.CONTENT);
			DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] { debugEvent });
		}
	}

}
