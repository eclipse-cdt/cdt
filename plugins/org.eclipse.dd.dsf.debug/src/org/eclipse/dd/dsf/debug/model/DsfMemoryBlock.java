/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson Communication  - upgrade IF from IMemoryBlock to IMemoryBlockExtension
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.model;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.utils.Addr32;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.Query;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.debug.service.IMemory;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.MemoryByte;

/**
 * This class holds the memory block retrieved from the target as a result of
 * a getBytes() or getBytesFromAddress() call from the platform.
 */
public class DsfMemoryBlock extends PlatformObject implements IMemoryBlockExtension 
{
    private final DsfMemoryBlockRetrieval fRetrieval;
    private final String fModelId;
    private final String fExpression;
    private final long fStartAddress;
    private final long fLength;
    private final BigInteger fBaseAddress;
    
    /**
     * Constructor
     * 
     * @param retrieval
     * @param modelId
     * @param expression
     * @param startAddress
     * @param length
     */
    DsfMemoryBlock(DsfMemoryBlockRetrieval retrieval, String modelId, String expression, long startAddress, long length) {
        fRetrieval = retrieval;
        fModelId = modelId;
        fExpression = expression;
        fStartAddress = startAddress;
        fBaseAddress = new BigInteger(Long.toString(startAddress));
        fLength = length;
    }

    // ////////////////////////////////////////////////////////////////////////
    // IAdaptable
    // ////////////////////////////////////////////////////////////////////////

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
     */
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
        return null;
    }

    // ////////////////////////////////////////////////////////////////////////
    // IMemoryBock interface - obsoleted by IMemoryBlockExtension
    // ////////////////////////////////////////////////////////////////////////

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IMemoryBlock#getStartAddress()
     */
    public long getStartAddress() {
        return fStartAddress;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IMemoryBlock#getLength()
     */
    public long getLength() {
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
        for (int i = 0; i < length; i++)
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
    	setValue(BigInteger.valueOf(offset), bytes);
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
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getMemoryBlockEndAddress()
	 */
	public BigInteger getMemoryBlockEndAddress() throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getBigLength()
	 */
	public BigInteger getBigLength() throws DebugException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getAddressSize()
	 */
	public int getAddressSize() throws DebugException {
		// TODO: Have the service make a trip to the back-end and
		// retrieve/store that information for us
		return 4;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#supportBaseAddressModification()
	 */
	public boolean supportBaseAddressModification() throws DebugException {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#supportsChangeManagement()
	 */
	public boolean supportsChangeManagement() {
		return true;
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
	public MemoryByte[] getBytesFromOffset(BigInteger unitOffset, long addressableUnits) throws DebugException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getBytesFromAddress(java.math.BigInteger, long)
	 */
	public MemoryByte[] getBytesFromAddress(BigInteger address, long units) throws DebugException {
		return fetchMemoryBlock(address.longValue(), units);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#setValue(java.math.BigInteger, byte[])
	 */
	public void setValue(BigInteger offset, byte[] bytes) throws DebugException {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
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
		// TODO: Have the service make a trip to the back-end and
		// retrieve/store that information for us
		return 1;
	}

    // ////////////////////////////////////////////////////////////////////////
    // Helper functions
    // ////////////////////////////////////////////////////////////////////////

    /* The real thing. Since the original call is synchronous (from a platform Job),
     * we use a Query that will patiently wait for the underlying asynchronous calls
     * to complete before returning.
     * 
     * @param address
     * @param length
     * @return MemoryBytes[]
     * @throws DebugException
     */
    private MemoryByte[] fetchMemoryBlock(final long address, final long length) throws DebugException {

    	/* For this first implementation, we make a few simplifying assumptions:
    	 * - word_size = 1 (we want to read individual bytes)
    	 * - offset = 0
    	 * - mode = hexadecimal (will be overridden in getMemory() anyway)
    	 */
    	final int word_size = 1;
    	final int offset = 0;
    	final int mode = 0;			// TODO: Add a constant for hexadecimal mode

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
			          fRetrieval.getContext(), new Addr32(address), word_size, buffer, offset, (int) length, mode,
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

}
