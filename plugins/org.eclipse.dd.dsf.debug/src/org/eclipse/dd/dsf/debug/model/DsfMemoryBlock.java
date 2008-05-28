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
 *     Ericsson Communication - added support for changed bytes
 *     Ericsson Communication - better management of exceptions
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.Query;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.debug.internal.DsfDebugPlugin;
import org.eclipse.dd.dsf.debug.internal.provisional.model.IMemoryBlockUpdatePolicyProvider;
import org.eclipse.dd.dsf.debug.service.IMemory;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IMemory.IMemoryChangedEvent;
import org.eclipse.dd.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.StateChangeReason;
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
public class DsfMemoryBlock extends PlatformObject implements IMemoryBlockExtension, IMemoryBlockUpdatePolicyProvider
{
	private final static String UPDATE_POLICY_AUTOMATIC = "Automatic"; //$NON-NLS-1$
	private final static String UPDATE_POLICY_MANUAL = "Manual"; //$NON-NLS-1$
	private final static String UPDATE_POLICY_BREAKPOINT = "On Breakpoint"; //$NON-NLS-1$
	
	private final IMemoryDMContext fContext;
    private final ILaunch fLaunch;
    private final IDebugTarget fDebugTarget;
    private final DsfMemoryBlockRetrieval fRetrieval;
    private final String fModelId;
    private final String fExpression;
    private final BigInteger fBaseAddress;

    private BigInteger fBlockAddress;
    private int fLength;
    private int fWordSize;
    private MemoryByte[] fBlock;
    
    private String fUpdatePolicy = UPDATE_POLICY_AUTOMATIC;
    
    private ArrayList<Object> fConnections = new ArrayList<Object>();

    @SuppressWarnings("unused")
	private boolean isEnabled;

    /**
     * Constructor.
     * 
     * @param retrieval    - the MemoryBlockRetrieval (session context)
     * @param modelId      - 
     * @param expression   - the displayed expression in the UI
     * @param address      - the actual memory block start address
     * @param word_size    - the number of bytes per address
     * @param length       - the requested block length (could be 0)
     */
    DsfMemoryBlock(DsfMemoryBlockRetrieval retrieval, IMemoryDMContext context, String modelId, String expression, BigInteger address, int word_size, long length) {
    	fLaunch      = retrieval.getLaunch();
    	fDebugTarget = retrieval.getDebugTarget();
        fRetrieval   = retrieval;
        fContext     = context;
        fModelId     = modelId;
        fExpression  = expression;
        fBaseAddress = address;

        // Current block information
        fBlockAddress = address;
        fWordSize     = word_size;
        fLength       = (int) length;
        fBlock        = null;

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
    	return fRetrieval.supportsValueModification();
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
		return fRetrieval.getAddressSize();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#supportBaseAddressModification()
	 */
	public boolean supportBaseAddressModification() throws DebugException {
		return fRetrieval.supportBaseAddressModification();
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
		fBlockAddress = address;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getBytesFromOffset(java.math.BigInteger, long)
	 */
	public MemoryByte[] getBytesFromOffset(BigInteger offset, long units) throws DebugException {
		return getBytesFromAddress(fBlockAddress.add(offset), units);
	}
	
	private boolean fUseCachedData = false;
	
	public void clearCache() {
		fUseCachedData = false;
	}
	
	@DsfServiceEventHandler 
    public void handleCacheSuspendEvent(IRunControl.ISuspendedDMEvent e) {
		e.getReason();
		if(e.getReason() == StateChangeReason.BREAKPOINT)
			fUseCachedData = false;
	}
	
	private boolean isUseCacheData()
	{
		if(fUpdatePolicy.equals(DsfMemoryBlock.UPDATE_POLICY_BREAKPOINT))
			return fUseCachedData;
		else if(fUpdatePolicy.equals(DsfMemoryBlock.UPDATE_POLICY_MANUAL))
			return fUseCachedData;
		else
			return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockExtension#getBytesFromAddress(java.math.BigInteger, long)
	 */
	public MemoryByte[] getBytesFromAddress(BigInteger address, long units) throws DebugException {
		if(isUseCacheData() && fBlockAddress.compareTo(address) == 0 && units * getAddressableSize() <= fBlock.length)
			return fBlock;
		
		MemoryByte[] block = fetchMemoryBlock(address, units);
		int newLength = (block != null) ? block.length : 0;

		// Flag the changed bytes
		if (fBlock != null && newLength > 0) {
			switch (fBlockAddress.compareTo(address))	{
				case -1:
				{
					int offset = address.subtract(fBlockAddress).intValue();
					int length = Math.min(fLength - offset, newLength); 
					for (int i = 0; i < length; i += 4) {
						boolean changed = false;
						for (int j = i; j < (i + 4) && j < length; j++) {
							block[j].setFlags(fBlock[offset + j].getFlags());
							if (block[j].getValue() != fBlock[offset + j].getValue())
								changed = true;
						}
						if (changed)
							for (int j = i; j < (i + 4) && j < length; j++) {
								block[j].setHistoryKnown(true);
								block[j].setChanged(true);
							}
					}
					break;
				}

				case 0:
				case 1:
				{
					int offset = fBlockAddress.subtract(address).intValue();
					int length = Math.min(newLength - offset, fLength); 
					for (int i = 0; i < length; i += 4) {
						boolean changed = false;
						for (int j = i; j < (i + 4) && j < length; j++) {
							block[offset + j].setFlags(fBlock[j].getFlags());
							if (block[offset + j].getValue() != fBlock[j].getValue())
								changed = true;
						}
						if (changed)
							for (int j = i; j < (i + 4) && j < length; j++) {
								block[offset + j].setHistoryKnown(true);
								block[offset + j].setChanged(true);
							}
					}
					break;
				}

				default:
					break;
			}
		}

		// Update the internal state
		fBlock = block;
		fBlockAddress = address;
		fLength = newLength;
		
		if(fUpdatePolicy.equals(DsfMemoryBlock.UPDATE_POLICY_BREAKPOINT))
			fUseCachedData = true;
		else if(fUpdatePolicy.equals(DsfMemoryBlock.UPDATE_POLICY_MANUAL))
			fUseCachedData = true;

		return fBlock;
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
		    // Session is down.
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
		return fRetrieval.getAddressableSize();
	}

    ///////////////////////////////////////////////////////////////////////////
    // Helper functions
    ///////////////////////////////////////////////////////////////////////////

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
    	
        // Use a Query to synchronize the downstream calls  
        Query<MemoryByte[]> query = new Query<MemoryByte[]>() {
			@Override
			protected void execute(final DataRequestMonitor<MemoryByte[]> drm) {
			    IMemory memoryService = (IMemory) fRetrieval.getServiceTracker().getService();
			    if (memoryService != null) {
			        // Go for it
			        memoryService.getMemory( 
			            fContext, address, 0, fWordSize, (int) length,
			            new DataRequestMonitor<MemoryByte[]>(fRetrieval.getExecutor(), drm) {
			                @Override
			                protected void handleSuccess() {
			                    drm.setData(getData());
			                    drm.done();
			                }
			    			@Override
			    			protected void handleFailure() {
			    				// Bug234289: If memory read fails, return a block marked as invalid
			    				MemoryByte[] block = new MemoryByte[fWordSize * (int) length];
		    					for (int i = 0; i < block.length; i++)
		    						block[i] = new MemoryByte((byte) 0, (byte) 0);
			    				drm.setData(block);
			    				drm.done();
			    			}
			            });
			    }
				
			}
        };
        fRetrieval.getExecutor().execute(query);

		try {
            return query.get();
        } catch (InterruptedException e) {
    		throw new DebugException(new Status(IStatus.ERROR,
    				DsfDebugPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR,
    				"Error reading memory block (InterruptedException)", e)); //$NON-NLS-1$
        } catch (ExecutionException e) {
    		throw new DebugException(new Status(IStatus.ERROR,
    				DsfDebugPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR,
    				"Error reading memory block (ExecutionException)", e)); //$NON-NLS-1$
        }
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

        // Use a Query to synchronize the downstream calls  
        Query<MemoryByte[]> query = new Query<MemoryByte[]>() {
			@Override
			protected void execute(final DataRequestMonitor<MemoryByte[]> drm) {
			    IMemory memoryService = (IMemory) fRetrieval.getServiceTracker().getService();
			    if (memoryService != null) {
			        // Go for it
	    	        memoryService.setMemory(
		    	  	      fContext, address, offset, fWordSize, bytes.length, bytes,
		    	  	      new RequestMonitor(fRetrieval.getExecutor(), null));
			    }
				
			}
        };
        fRetrieval.getExecutor().execute(query);

		try {
            query.get();
        } catch (InterruptedException e) {
    		throw new DebugException(new Status(IStatus.ERROR,
    				DsfDebugPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR,
    				"Error writing memory block (InterruptedException)", e)); //$NON-NLS-1$
        } catch (ExecutionException e) {
    		throw new DebugException(new Status(IStatus.ERROR,
    				DsfDebugPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR,
    				"Error writing memory block (ExecutionException)", e)); //$NON-NLS-1$
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Event Handlers
    ///////////////////////////////////////////////////////////////////////////

    @DsfServiceEventHandler 
    public void eventDispatched(IRunControl.ISuspendedDMEvent e) {

    	// Clear the "Changed" flags after each run/resume/step
		for (int i = 0; i < fLength; i++)
			fBlock[i].setChanged(false);
    	
    	// Generate the MemoryChangedEvents
        handleMemoryChange(BigInteger.ZERO);
    }
    
    @DsfServiceEventHandler
    public void eventDispatched(IMemoryChangedEvent e) {

        // Check if we are in the same address space 
        if (e.getDMContext().equals(fContext)) {
        	IAddress[] addresses = e.getAddresses();
        	for (int i = 0; i < addresses.length; i++)
        		handleMemoryChange(addresses[i].getValue());
        }
    }
    
    /**
	 * @param address
	 * @param length
	 */
	public void handleMemoryChange(BigInteger address) {
		
		// Check if the change affects this particular block (0 is universal)
		BigInteger fEndAddress = fBlockAddress.add(BigInteger.valueOf(fLength));
		if (address.equals(BigInteger.ZERO) ||
		   ((fBlockAddress.compareTo(address) != 1) && (fEndAddress.compareTo(address) == 1)))
		{
			// Notify the event listeners
			DebugEvent debugEvent = new DebugEvent(this, DebugEvent.CHANGE, DebugEvent.CONTENT);
			DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] { debugEvent });
		}
	}

	public String[] getUpdatePolicies() {
		return new String[] {UPDATE_POLICY_AUTOMATIC, UPDATE_POLICY_MANUAL, UPDATE_POLICY_BREAKPOINT};
	}
    
    public String getUpdatePolicy()
    {
    	return fUpdatePolicy;
    }
    
    public void setUpdatePolicy(String policy)
    {
    	fUpdatePolicy = policy;
    }
    
    public String getUpdatePolicyDescription(String id) {
		return id;
	}
}
