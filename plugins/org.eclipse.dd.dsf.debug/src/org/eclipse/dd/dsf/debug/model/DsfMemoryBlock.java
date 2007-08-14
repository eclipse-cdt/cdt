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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

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
    private final DsfMemoryBlockRetrieval fRetrieval;
    private final String       fModelId;
    private final String       fExpression;
    private final BigInteger   fStartAddress;
    private       BigInteger   fEndAddress;
    private       long         fLength = 0;

    /**
     * Constructor.
     * 
     * @param retrieval    - the MemoryBlockRetrieval (session context)
     * @param modelId      - 
     * @param expression   - the displayed expression
     * @param startAddress - the actual memory block start address
     * @param length       - the memory block length (could be 0)
     */
    DsfMemoryBlock(DsfMemoryBlockRetrieval retrieval, String modelId, String expression, BigInteger startAddress, long length) {
        fRetrieval    = retrieval;
        fModelId      = modelId;
        fExpression   = expression;
        fStartAddress = startAddress;
        fEndAddress   = startAddress.add(BigInteger.valueOf(length));
        fLength       = length;
        
        try {
            fRetrieval.getExecutor().execute(new Runnable() {
                public void run() {
                    fRetrieval.getSession().addServiceEventListener(DsfMemoryBlock.this, null);
                }
            });
        } catch(RejectedExecutionException e) {
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
    	// FRCH: return fRetrieval.getDebugTarget();
        return null;
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
    	// FRCH: return fRetrieval.getDebugTarget().getLaunch();
        return null;
    }

    // ////////////////////////////////////////////////////////////////////////
    // IMemoryBock interface - obsoleted by IMemoryBlockExtension
    // ////////////////////////////////////////////////////////////////////////

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IMemoryBlock#getStartAddress()
     */
    public long getStartAddress() {
    	// Warning: doesn't support 64-bit addresses
    	long address = fStartAddress.longValue();
    	if (fStartAddress.equals(BigInteger.valueOf(address)))
    		return address;

    	// FRCH: Should we throw an exception instead?
    	return 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IMemoryBlock#getLength()
     */
    public long getLength() {
    	// Warning: could return 0 
        return fLength;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IMemoryBlock#getBytes()
     */
    public byte[] getBytes() throws DebugException {
    	MemoryByte[] block = fetchMemoryBlock(fStartAddress, fLength);
    	int length = block.length;
        byte[] bytes = new byte[length];
        // Extract bytes from MemoryBytes
        for (int i : bytes)
        	bytes[i] = block[i].getValue();
        return bytes;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IMemoryBlock#supportsValueModification()
     */
    public boolean supportsValueModification() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IMemoryBlock#setValue(long, byte[])
     */
    public void setValue(long offset, byte[] bytes) throws DebugException {
    	if (offset <= Integer.MAX_VALUE)
	    	writeMemoryBlock((int) offset, bytes);

    	// FRCH: Should we throw an exception if offset is too large?
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
        return fStartAddress;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getMemoryBlockStartAddress()
	 */
	public BigInteger getMemoryBlockStartAddress() throws DebugException {
		// "null" indicates that memory can be retrieved at addresses lower than the block base address
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getMemoryBlockEndAddress()
	 */
	public BigInteger getMemoryBlockEndAddress() throws DebugException {
		// "null" indicates that memory can be retrieved at addresses higher the block base address
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getBigLength()
	 */
	public BigInteger getBigLength() throws DebugException {
		// -1 indicates that memory block is unbounded
		return BigInteger.ONE.negate();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getAddressSize()
	 */
	public int getAddressSize() throws DebugException {
		// FRCH: return fRetrieval.getDebugTarget().getAddressSize();
		return 4;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#supportBaseAddressModification()
	 */
	public boolean supportBaseAddressModification() throws DebugException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#supportsChangeManagement()
	 */
	public boolean supportsChangeManagement() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#setBaseAddress(java.math.BigInteger)
	 */
	public void setBaseAddress(BigInteger address) throws DebugException {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getBytesFromOffset(java.math.BigInteger, long)
	 */
	public MemoryByte[] getBytesFromOffset(BigInteger offset, long units) throws DebugException {
		return getBytesFromAddress(fStartAddress.add(offset), units);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getBytesFromAddress(java.math.BigInteger, long)
	 */
	public MemoryByte[] getBytesFromAddress(BigInteger address, long units) throws DebugException {
		fLength = units;
		fEndAddress = fStartAddress.add(BigInteger.valueOf(units));
		return fetchMemoryBlock(address, units);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#setValue(java.math.BigInteger, byte[])
	 */
	public void setValue(BigInteger offset, byte[] bytes) throws DebugException {
		// Ensure that the offset can be cast into an int 
    	int offs = offset.intValue();
    	if (offset.equals(BigInteger.valueOf(offs)))
    		writeMemoryBlock(offs, bytes);

    	// FRCH: Should we throw an exception if offset is too large?
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#connect(java.lang.Object)
	 */
	public void connect(Object client) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#disconnect(java.lang.Object)
	 */
	public void disconnect(Object client) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getConnections()
	 */
	public Object[] getConnections() {
		// TODO Auto-generated method stub
		return new Object[0];
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
		} catch(RejectedExecutionException e) {
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
		// FRCH: return fRetrieval.getDebugTarget().getAddressableSize();
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
	 * @param address @param length @return MemoryByte[] @throws DebugException
	 */
    private MemoryByte[] fetchMemoryBlock(final BigInteger address, final long length) throws DebugException {

    	/* FRCH: Fix the loose ends...
    	 * 
    	 * For this first implementation, we make a few simplifying assumptions:
    	 * - word_size = 1 (we want to read individual bytes)
    	 * - offset    = 0
    	 * - mode      = 0 (will be overridden in getMemory() anyway)
    	 */
    	final int word_size = 1;
    	final int offset    = 0;
    	final int mode      = 0;

        // Use a Query to "synchronize" the inherently asynchronous downstream calls  
        Query<MemoryByte[]> query = new Query<MemoryByte[]>() {
			@Override
			protected void execute(final DataRequestMonitor<MemoryByte[]> rm) {
			    IMemory memoryService = (IMemory) fRetrieval.getServiceTracker().getService();
			    if (memoryService != null) {
			    	// Place holder for the result
			        final MemoryByte[] buffer = new MemoryByte[(int) length];
			        // Go for it
			        memoryService.getMemory( 
			          fRetrieval.getContext(), address, word_size, buffer, offset, (int) length, mode,
			          new RequestMonitor(fRetrieval.getExecutor(), rm) {
			              @Override
			              protected void handleOK() {
			                  rm.setData(buffer);
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
        handleMemoryChange(e.getAddress());
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
    private void writeMemoryBlock(final int offset, final byte[] bytes) throws DebugException {

    	final int word_size = 1;
    	final int mode      = 0;

    	IMemory memoryService = (IMemory) fRetrieval.getServiceTracker().getService();
	    if (memoryService != null) {
	        memoryService.setMemory(
	          fRetrieval.getContext(), fStartAddress, word_size, bytes, offset, bytes.length, mode,
	          new RequestMonitor(fRetrieval.getExecutor(), null) {
	              @Override
	              protected void handleOK() {
	            	  handleMemoryChange(fStartAddress);
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
		if (address.equals(BigInteger.ZERO) ||
		   ((fStartAddress.compareTo(address) != 1) && (fEndAddress.compareTo(address) == 1)))
		{
			// Notify the event listeners
			DebugEvent debugEvent = new DebugEvent(this, DebugEvent.CHANGE, DebugEvent.CONTENT);
			DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] { debugEvent });
		}
	}

}
