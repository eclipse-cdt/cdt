/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson AB - expanded from initial stub
 *     Ericsson AB - added support for event handling
 *     Ericsson AB - added memory cache
 *     Vladimir Prus (CodeSourcery) - support for -data-read-memory-bytes (bug 322658)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMAddress;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerSuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.debug.service.command.BufferedCommandControl;
import org.eclipse.cdt.dsf.debug.service.command.CommandCache;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.MIExpressions.ExpressionChangedEvent;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataReadMemoryBytesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataReadMemoryInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataWriteMemoryInfo;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.MemoryByte;
import org.osgi.framework.BundleContext;

/**
 * Memory service implementation
 */
public class MIMemory extends AbstractDsfService implements IMemory, ICachingService {

	private static final String READ_MEMORY_BYTES_FEATURE = "data-read-memory-bytes"; //$NON-NLS-1$
	
    public class MemoryChangedEvent extends AbstractDMEvent<IMemoryDMContext> 
        implements IMemoryChangedEvent 
    {
        IAddress[] fAddresses;
        IDMContext fContext;
        
        public MemoryChangedEvent(IMemoryDMContext context, IAddress[] addresses) {
            super(context);
            fAddresses = addresses;
        }

        public IAddress[] getAddresses() {
            return fAddresses;
        }
    }

	// Back-end commands cache
	private CommandCache fCommandCache;
	private CommandFactory fCommandFactory;

	// Map of memory caches
    private Map<IMemoryDMContext, MIMemoryCache> fMemoryCaches;

    private MIMemoryCache getMemoryCache(IMemoryDMContext memoryDMC) {
    	MIMemoryCache cache = fMemoryCaches.get(memoryDMC);
    	if (cache == null) {
    		cache = new MIMemoryCache();
    		fMemoryCaches.put(memoryDMC, cache);
    	}
    	return cache;
    }
    
    // Whether the -data-read-memory-bytes should be used
    // instead of -data-read-memory
    private boolean fDataReadMemoryBytes;
    
	/**
	 *  Constructor 
	 */
	public MIMemory(DsfSession session) {
		super(session);
    }

    ///////////////////////////////////////////////////////////////////////////
    // AbstractDsfService overrides
    ///////////////////////////////////////////////////////////////////////////

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.service.AbstractDsfService#initialize(org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 * 
	 * This function is called during the launch sequence (where the service is 
	 * instantiated). See LaunchSequence.java.
	 */
	@Override
    public void initialize(final RequestMonitor requestMonitor) {
        super.initialize(new RequestMonitor(getExecutor(), requestMonitor) {
            @Override
            protected void handleSuccess() {
                doInitialize(requestMonitor);
            }
        });
    }

    /*
     * Initialization function:
     * - Register the service
     * - Create the command cache
     * - Register self to service events
     * 
     * @param requestMonitor
     */
    private void doInitialize(final RequestMonitor requestMonitor) {
    	// Create the command cache
        IGDBControl commandControl = getServicesTracker().getService(IGDBControl.class);
        BufferedCommandControl bufferedCommandControl = new BufferedCommandControl(commandControl, getExecutor(), 2);
		
    	fDataReadMemoryBytes = commandControl.getFeatures().contains(READ_MEMORY_BYTES_FEATURE);
    	
        fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();

		// This cache stores the result of a command when received; also, this cache
		// is manipulated when receiving events.  Currently, events are received after
		// three scheduling of the executor, while command results after only one.  This
		// can cause problems because command results might be processed before an event
		// that actually arrived before the command result.
		// To solve this, we use a bufferedCommandControl that will delay the command
		// result by two scheduling of the executor.
		// See bug 280461
    	fCommandCache = new CommandCache(getSession(), bufferedCommandControl);
    	fCommandCache.setContextAvailable(commandControl.getContext(), true);

    	// Register this service
    	register(new String[] { MIMemory.class.getName(), IMemory.class.getName() }, new Hashtable<String, String>());

    	// Create the memory requests cache
    	fMemoryCaches = new HashMap<IMemoryDMContext, MIMemoryCache>();

		// Register as service event listener
    	getSession().addServiceEventListener(this, null);

    	// Done 
    	requestMonitor.done();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.service.AbstractDsfService#shutdown(org.eclipse.cdt.dsf.concurrent.RequestMonitor)
     */
    @Override
    public void shutdown(final RequestMonitor requestMonitor) {

    	// Unregister this service
        unregister();

		// Remove event listener
    	getSession().removeServiceEventListener(this);

    	// Complete the shutdown
        super.shutdown(requestMonitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.service.AbstractDsfService#getBundleContext()
     */
    @Override
    protected BundleContext getBundleContext() {
        return GdbPlugin.getBundleContext();
    }

    ///////////////////////////////////////////////////////////////////////////
    // IMemory
    ///////////////////////////////////////////////////////////////////////////

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.service.IMemory#getMemory(org.eclipse.cdt.dsf.datamodel.IDMContext, org.eclipse.cdt.core.IAddress, long, int, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
     */
    public void getMemory(IMemoryDMContext memoryDMC, IAddress address, long offset,
    		int word_size, int count, DataRequestMonitor<MemoryByte[]> drm)
	{
        // Validate the context
        if (memoryDMC == null) {
            drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Unknown context type", null)); //$NON-NLS-1$);
            drm.done();            
            return;
        }

        // Validate the word size
    	// NOTE: We only accept 1 byte words for this implementation
    	if (word_size != 1) {
    		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Word size not supported (!= 1)", null)); //$NON-NLS-1$
    		drm.done();
    		return;
    	}

    	// Validate the byte count
    	if (count < 0) {
    		drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Invalid word count (< 0)", null)); //$NON-NLS-1$
    		drm.done();
    		return;
    	}

    	// All is clear: go for it
    	getMemoryCache(memoryDMC).getMemory(memoryDMC, address.add(offset), word_size, count, drm);
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.service.IMemory#setMemory(org.eclipse.cdt.dsf.datamodel.IDMContext, org.eclipse.cdt.core.IAddress, long, int, byte[], org.eclipse.cdt.dsf.concurrent.RequestMonitor)
     */
    public void setMemory(IMemoryDMContext memoryDMC, IAddress address, long offset,
    		int word_size, int count, byte[] buffer, RequestMonitor rm)
    {
        // Validate the context
        if (memoryDMC == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Unknown context type", null)); //$NON-NLS-1$);
            rm.done();            
            return;
        }

    	// Validate the word size
    	// NOTE: We only accept 1 byte words for this implementation
    	if (word_size != 1) {
    		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Word size not supported (!= 1)", null)); //$NON-NLS-1$
    		rm.done();
    		return;
    	}

    	// Validate the byte count
    	if (count < 0) {
    		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Invalid word count (< 0)", null)); //$NON-NLS-1$
    		rm.done();
    		return;
    	}

    	// Validate the buffer size
    	if (buffer.length < count) {
    		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Buffer too short", null)); //$NON-NLS-1$
    		rm.done();
    		return;
    	}

    	// All is clear: go for it
    	getMemoryCache(memoryDMC).setMemory(memoryDMC, address, offset, word_size, count, buffer, rm);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.service.IMemory#fillMemory(org.eclipse.cdt.dsf.datamodel.IDMContext, org.eclipse.cdt.core.IAddress, long, int, byte[], org.eclipse.cdt.dsf.concurrent.RequestMonitor)
     */
    public void fillMemory(IMemoryDMContext memoryDMC, IAddress address, long offset,
    		int word_size, int count, byte[] pattern, RequestMonitor rm)
    {
        // Validate the context
        if (memoryDMC == null) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Unknown context type", null)); //$NON-NLS-1$);
            rm.done();            
            return;
        }

    	// Validate the word size
    	// NOTE: We only accept 1 byte words for this implementation
    	if (word_size != 1) {
    		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Word size not supported (!= 1)", null)); //$NON-NLS-1$
    		rm.done();
    		return;
    	}

    	// Validate the repeat count
    	if (count < 0) {
    		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Invalid repeat count (< 0)", null)); //$NON-NLS-1$
    		rm.done();
    		return;
    	}

    	// Validate the pattern
    	if (pattern.length < 1) {
    		rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR, "Empty pattern", null)); //$NON-NLS-1$
    		rm.done();
    		return;
    	}

    	// Create an aggregate buffer so we can write in 1 shot
    	int length = pattern.length;
    	byte[] buffer = new byte[count * length];
    	for (int i = 0; i < count; i++) {
    		System.arraycopy(pattern, 0, buffer, i * length, length);
    	}

    	// All is clear: go for it
    	getMemoryCache(memoryDMC).setMemory(memoryDMC, address, offset, word_size, count * length, buffer, rm);
    }

    ///////////////////////////////////////////////////////////////////////
    // Back-end functions 
    ///////////////////////////////////////////////////////////////////////

    /**
     * @param memoryDMC
     * @param address
     * @param offset
     * @param word_size
     * @param count
     * @param drm
     * 
     * @since 1.1
     */
    protected void readMemoryBlock(IDMContext dmc, IAddress address, final long offset,
    		final int word_size, final int count, final DataRequestMonitor<MemoryByte[]> drm)
    {
    	if (fDataReadMemoryBytes) {
    		fCommandCache.execute(
    			fCommandFactory.createMIDataReadMemoryBytes(dmc, address.toString(), offset*word_size, count*word_size),
    			new DataRequestMonitor<MIDataReadMemoryBytesInfo>(getExecutor(), drm) {
    				@Override
    				protected void handleSuccess() {
    					// Retrieve the memory block
    					drm.setData(getData().getMIMemoryBlock());
    					drm.done();
    				}
    				@Override
    				protected void handleFailure() {
    					drm.setData(createInvalidBlock(word_size * count));
    					drm.done();
    				}    					
    			});
    	} else {
    		/* To simplify the parsing of the MI result, we request the output to
    		 * be on 1 row of [count] columns, no char interpretation.
    		 */
    		int mode = MIFormat.HEXADECIMAL;
    		int nb_rows = 1;
    		int nb_cols = count;
    		Character asChar = null;

    		fCommandCache.execute(
    			fCommandFactory.createMIDataReadMemory(dmc, offset, address.toString(), mode, word_size, nb_rows, nb_cols, asChar),
    			new DataRequestMonitor<MIDataReadMemoryInfo>(getExecutor(), drm) {
    				@Override
    				protected void handleSuccess() {
    					// Retrieve the memory block
    					drm.setData(getData().getMIMemoryBlock());
    					drm.done();
    				}
    				@Override
    				protected void handleFailure() {
    					drm.setData(createInvalidBlock(word_size * count));
    					drm.done();
    				}
    			}
    		);
    	}
    }
    
	private MemoryByte[] createInvalidBlock(int size) {
		// Bug234289: If memory read fails, return a block marked as invalid
		MemoryByte[] block = new MemoryByte[size];
		for (int i = 0; i < block.length; i++)
			block[i] = new MemoryByte((byte) 0, (byte) 0);
		return block;
	}

    /**
     * @param memoryDMC
     * @param address
     * @param offset
     * @param word_size
     * @param count
     * @param buffer
     * @param rm
     * 
     * @since 1.1
     */
    protected void writeMemoryBlock(final IDMContext dmc, final IAddress address, final long offset,
    		final int word_size, final int count, final byte[] buffer, final RequestMonitor rm)
    {
    	// Each byte is written individually (GDB power...)
    	// so we need to keep track of the count
    	final CountingRequestMonitor countingRM = new CountingRequestMonitor(getExecutor(), rm);
    	countingRM.setDoneCount(count);

    	// We will format the individual bytes in decimal
    	int format = MIFormat.DECIMAL;
    	String baseAddress = address.toString();

    	// Issue an MI request for each byte to write
    	for (int i = 0; i < count; i++) {
    		String value = new Byte(buffer[i]).toString();
    		fCommandCache.execute(
    				fCommandFactory.createMIDataWriteMemory(dmc, offset + i, baseAddress, format, word_size, value),
    				new DataRequestMonitor<MIDataWriteMemoryInfo>(getExecutor(), countingRM)
    		);
    	}
    }

    //////////////////////////////////////////////////////////////////////////
    // Event handlers
    //////////////////////////////////////////////////////////////////////////

    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler
	public void eventDispatched(IResumedDMEvent e) {
    	if (e instanceof IContainerResumedDMEvent) {
    		fCommandCache.setContextAvailable(e.getDMContext(), false);
    	}
    	
   		if (e.getReason() != StateChangeReason.STEP) {
	    	fCommandCache.reset(e.getDMContext());
	    	IMemoryDMContext memoryDMC = DMContexts.getAncestorOfType(e.getDMContext(), IMemoryDMContext.class);
	    	if (fMemoryCaches.containsKey(memoryDMC)) {
	    		// We do not want to use the call to getMemoryCache() here.
	    		// This is because:
	    		// 1- if there is not an entry already , we do not want to automatically 
	    		//    create one, just to call reset() on it.
	    		// 2- if memoryDMC == null, we do not want to create a cache
	    		//    entry for which the key is 'null'
	    		fMemoryCaches.get(memoryDMC).reset();
	    	}
   		}
	}
   
    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler
	public void eventDispatched(ISuspendedDMEvent e) {
    	if (e instanceof IContainerSuspendedDMEvent) {
    		fCommandCache.setContextAvailable(e.getDMContext(), true);
    	}
    	
    	fCommandCache.reset(e.getDMContext());
    	IMemoryDMContext memoryDMC = DMContexts.getAncestorOfType(e.getDMContext(), IMemoryDMContext.class);
    	if (fMemoryCaches.containsKey(memoryDMC)) {
    		// We do not want to use the call to getMemoryCache() here.
    		// This is because:
    		// 1- if there is not an entry already , we do not want to automatically 
    		//    create one, just to call reset() on it.
    		// 2- if memoryDMC == null, we do not want to create a cache
    		//    entry for which the key is 'null'
    		fMemoryCaches.get(memoryDMC).reset();
    	}
	}

    /**
     * @nooverride This method is not intended to be re-implemented or extended by clients.
     * @noreference This method is not intended to be referenced by clients.
     */
   	@DsfServiceEventHandler
	public void eventDispatched(ExpressionChangedEvent e) {

   		// Get the context and expression service handle
   		final IExpressionDMContext context = e.getDMContext();
		IExpressions expressionService = getServicesTracker().getService(IExpressions.class);

		// Get the variable information and update the corresponding memory locations
		if (expressionService != null) {
			expressionService.getExpressionAddressData(context,
				new DataRequestMonitor<IExpressionDMAddress>(getExecutor(), null) {
					@Override
					protected void handleSuccess() {
						// Figure out which memory area was modified
						IExpressionDMAddress expression = getData();
						final int count = expression.getSize();
						IAddress expAddress = expression.getAddress();
						final Addr64 address;
						if (expAddress instanceof Addr64)
							address = (Addr64) expAddress;
						else
							address = new Addr64(expAddress.getValue());

						final IMemoryDMContext memoryDMC = DMContexts.getAncestorOfType(context, IMemoryDMContext.class);
						getMemoryCache(memoryDMC).refreshMemory(memoryDMC, address, 0, 1, count,
								new RequestMonitor(getExecutor(), null));
						}
			});
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// SortedLinkedlist
	///////////////////////////////////////////////////////////////////////////

	// This class is really the equivalent of a C struct (old habits die hard...)
   	// For simplicity, everything is public.
   	private class MemoryBlock {
		public IAddress fAddress;
		public long fLength;
		public MemoryByte[] fBlock;
		public MemoryBlock(IAddress address, long length, MemoryByte[] block) {
			fAddress = address;
			fLength = length;
			fBlock = block;
		}
	}

   	// Address-ordered data structure to cache the memory blocks.
   	// Contiguous blocks are merged if possible.
	@SuppressWarnings("serial")
	private class SortedMemoryBlockList extends LinkedList<MemoryBlock> {

		public SortedMemoryBlockList() {
			super();
		}

		// Insert the block in the sorted linked list and merge contiguous
		// blocks if necessary
		@Override
		public boolean add(MemoryBlock block) {

			// If the list is empty, just store the block
			if (isEmpty()) {
				addFirst(block);
				return true;
			}

			// Insert the block at the correct location and then
			// merge the blocks if possible
			ListIterator<MemoryBlock> it = listIterator();
			while (it.hasNext()) {
				int index = it.nextIndex();
				MemoryBlock item = it.next();
				if (block.fAddress.compareTo(item.fAddress) < 0) {
					add(index, block);
					compact(index);
					return true;
				}
			}

			// Put at the end of the list and merge if necessary 
			addLast(block);
			compact(size() - 1);
			return true;
		}

		// Merge this block with its contiguous neighbors (if any)
		// Note: Merge is not performed if resulting block size would exceed MAXINT
		private void compact(int index) {

			MemoryBlock newBlock = get(index); 

			// Case where the block is to be merged with the previous block
			if (index > 0) {
				MemoryBlock prevBlock = get(index - 1);
				IAddress endOfPreviousBlock = prevBlock.fAddress.add(prevBlock.fLength);
				if (endOfPreviousBlock.distanceTo(newBlock.fAddress).longValue() == 0) {
					long newLength = prevBlock.fLength + newBlock.fLength;
					if (newLength <= Integer.MAX_VALUE) {
						MemoryByte[] block = new MemoryByte[(int) newLength] ;
						System.arraycopy(prevBlock.fBlock, 0, block, 0, (int) prevBlock.fLength);
						System.arraycopy(newBlock.fBlock, 0, block, (int) prevBlock.fLength, (int) newBlock.fLength);
						newBlock = new MemoryBlock(prevBlock.fAddress, newLength, block);
						remove(index);
						index -= 1;
						set(index, newBlock);
					}
				}
			}

			// Case where the block is to be merged with the following block
			int lastIndex = size() - 1;
			if (index < lastIndex) {
				MemoryBlock nextBlock = get(index + 1);
				IAddress endOfNewBlock = newBlock.fAddress.add(newBlock.fLength);
				if (endOfNewBlock.distanceTo(nextBlock.fAddress).longValue() == 0) {
					long newLength = newBlock.fLength + nextBlock.fLength;
					if (newLength <= Integer.MAX_VALUE) {
						MemoryByte[] block = new MemoryByte[(int) newLength] ;
						System.arraycopy(newBlock.fBlock, 0, block, 0, (int) newBlock.fLength);
						System.arraycopy(nextBlock.fBlock, 0, block, (int) newBlock.fLength, (int) nextBlock.fLength);
						newBlock = new MemoryBlock(newBlock.fAddress, newLength, block);
						set(index, newBlock);
						remove(index + 1);
					}
				}
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// MIMemoryCache
	///////////////////////////////////////////////////////////////////////////

	private class MIMemoryCache {
		// The memory cache data structure
		private SortedMemoryBlockList fMemoryBlockList;

		public MIMemoryCache() {
	    	// Create the memory block cache
	    	fMemoryBlockList = new SortedMemoryBlockList();
		}

		public void reset() {
	    	// Clear the memory cache
	    	fMemoryBlockList.clear();
		}

	    /**
 	     *  This function walks the address-sorted memory block list to identify
	     *  the 'missing' blocks (i.e. the holes) that need to be fetched on the target.
	     * 
	     *  The idea is fairly simple but an illustration could perhaps help.
	     *  Assume the cache holds a number of cached memory blocks with gaps i.e.
	     *  there is un-cached memory areas between blocks A, B and C:
	     * 
	     *        +---------+      +---------+      +---------+
	     *        +    A    +      +    B    +      +    C    +
	     *        +---------+      +---------+      +---------+
	     *        :         :      :         :      :         :
	     *   [a]  :         :  [b] :         :  [c] :         :  [d]
	     *        :         :      :         :      :         :
	     *   [e---+--]      :  [f--+---------+--]   :         :
	     *   [g---+---------+------+---------+------+---------+----]
	     *        :         :      :         :      :         :
	     *        :   [h]   :      :   [i----+--]   :         :
	     * 
	     * 
	     *  We have the following cases to consider.The requested block [a-i] either:
	     * 
	     *  [1] Fits entirely before A, in one of the gaps, or after C
	     *      with no overlap and no contiguousness (e.g. [a], [b], [c] and [d])
	     *      -> Add the requested block to the list of blocks to fetch
	     * 
	     *  [2] Starts before an existing block but overlaps part of it, possibly
	     *      spilling in the gap following the cached block (e.g. [e], [f] and [g])
	     *      -> Determine the length of the missing part (< count)
	     *      -> Add a request to fill the gap before the existing block
	     *      -> Update the requested block for the next iteration:
	     *         - Start address to point just after the end of the cached block
	     *         - Count reduced by cached block length (possibly becoming negative, e.g. [e])
	     *      At this point, the updated requested block starts just beyond the cached block
	     *      for the next iteration.
	     * 
	     *  [3] Starts at or into an existing block and overlaps part of it ([h] and [i])
	     *      -> Update the requested block for the next iteration:
	     *         - Start address to point just after the end of the cached block
	     *         - Count reduced by length to end of cached block (possibly becoming negative, e.g. [h])
	     *      At this point, the updated requested block starts just beyond the cached block
	     *      for the next iteration.
	     * 
	     *  We iterate over the cached blocks list until there is no entry left or until
	     *  the remaining requested block count is <= 0, meaning the result list contains
	     *  only the sub-blocks needed to fill the gap(s), if any.
	     * 
	     *  (As is often the case, it takes much more typing to explain it than to just do it :-)
	     *
	     *  What is missing is a parameter that indicates the minimal block size that is worth fetching.
	     *  This is target-specific and straight in the realm of the coalescing function... 
	     *  
	     * @param reqBlockStart The address of the requested block
	     * @param count Its length
	     * @return A list of the sub-blocks to fetch in order to fill enough gaps in the memory cache
	     * to service the request
	     */
	    private LinkedList<MemoryBlock> getListOfMissingBlocks(IAddress reqBlockStart, int count) {

			LinkedList<MemoryBlock> list = new LinkedList<MemoryBlock>();
			ListIterator<MemoryBlock> it = fMemoryBlockList.listIterator();

			// Look for holes in the list of memory blocks
			while (it.hasNext() && count > 0) {
				MemoryBlock cachedBlock = it.next();
				IAddress cachedBlockStart = cachedBlock.fAddress;
				IAddress cachedBlockEnd   = cachedBlock.fAddress.add(cachedBlock.fLength);

				// Case where we miss a block before the cached block
				if (reqBlockStart.distanceTo(cachedBlockStart).longValue() >= 0) {
					int length = (int) Math.min(reqBlockStart.distanceTo(cachedBlockStart).longValue(), count);
					// If both blocks start at the same location, no need to create a new cached block
					if (length > 0) {
						MemoryBlock newBlock = new MemoryBlock(reqBlockStart, length, new MemoryByte[0]);
						list.add(newBlock);
					}
					// Adjust request block start and length for the next iteration
					reqBlockStart = cachedBlockEnd;
					count -= length + cachedBlock.fLength;
				}

				// Case where the requested block starts somewhere in the cached block
				else if (cachedBlockStart.distanceTo(reqBlockStart).longValue() > 0
					&&  reqBlockStart.distanceTo(cachedBlockEnd).longValue() >= 0)
				{
					// Start of the requested block already in cache
					// Adjust request block start and length for the next iteration
					count -= reqBlockStart.distanceTo(cachedBlockEnd).longValue();
					reqBlockStart = cachedBlockEnd;
				}
			}

			// Case where we miss a block at the end of the cache
			if (count > 0) {
				MemoryBlock newBlock = new MemoryBlock(reqBlockStart, count, new MemoryByte[0]);
				list.add(newBlock);
			}
			
			return list;
		}

	    /**
	     *  This function walks the address-sorted memory block list to get the
	     *  cached memory bytes (possibly from multiple contiguous blocks).
	     *  This function is called *after* the missing blocks have been read from
	     *  the back end i.e. the requested memory is all cached. 
	     *
	     *  Again, this is fairly simple. As we loop over the address-ordered list,
	     *  There are really only 2 cases:
	     *
	     *  [1] The requested block fits entirely in the cached block ([a] or [b])
	     *  [2] The requested block starts in a cached block and ends in the
	     *      following (contiguous) one ([c]) in which case it is treated
	     *      as 2 contiguous requests ([c'] and [c"])
	     *
	     *       +--------------+--------------+
	     *       +       A      +      B       +
	     *       +--------------+--------------+
	     *       :  [a----]     :   [b-----]   :
	     *       :              :              :
	     *       :       [c-----+------]       :
	     *       :       [c'---]+[c"---]       :
		 *
	     * @param reqBlockStart The address of the requested block
	     * @param count Its length
	     * @return The cached memory content
	     */
	    private MemoryByte[] getMemoryBlockFromCache(IAddress reqBlockStart, int count) {

			IAddress reqBlockEnd = reqBlockStart.add(count);
			MemoryByte[] resultBlock = new MemoryByte[count];
			ListIterator<MemoryBlock> iter = fMemoryBlockList.listIterator();

			while (iter.hasNext()) {
				MemoryBlock cachedBlock = iter.next();
				IAddress cachedBlockStart = cachedBlock.fAddress;
				IAddress cachedBlockEnd   = cachedBlock.fAddress.add(cachedBlock.fLength);

				// Case where the cached block overlaps completely the requested memory block  
				if (cachedBlockStart.distanceTo(reqBlockStart).longValue() >= 0
					&& reqBlockEnd.distanceTo(cachedBlockEnd).longValue() >= 0)
				{
					int pos = (int) cachedBlockStart.distanceTo(reqBlockStart).longValue();
					System.arraycopy(cachedBlock.fBlock, pos, resultBlock, 0, count);
				}
				
				// Case where the beginning of the cached block is within the requested memory block  
				else if (reqBlockStart.distanceTo(cachedBlockStart).longValue() >= 0
					&& cachedBlockStart.distanceTo(reqBlockEnd).longValue() > 0)
				{
					int pos = (int) reqBlockStart.distanceTo(cachedBlockStart).longValue();
					int length = (int) Math.min(cachedBlock.fLength, count - pos);
					System.arraycopy(cachedBlock.fBlock, 0, resultBlock, pos, length);
				}
				
				// Case where the end of the cached block is within the requested memory block  
				else if (cachedBlockStart.distanceTo(reqBlockStart).longValue() >= 0
					&& reqBlockStart.distanceTo(cachedBlockEnd).longValue() > 0)
				{
					int pos = (int) cachedBlockStart.distanceTo(reqBlockStart).longValue();
					int length = (int) Math.min(cachedBlock.fLength - pos, count);
					System.arraycopy(cachedBlock.fBlock, pos, resultBlock, 0, length);
				}
 			}
			return resultBlock;
		}

		/**
	     *  This function walks the address-sorted memory block list and updates
	     *  the content with the actual memory just read from the target.
	     * 
		 * @param modBlockStart
		 * @param count
		 * @param modBlock
		 */
		private void updateMemoryCache(IAddress modBlockStart, int count, MemoryByte[] modBlock) {
			
			IAddress modBlockEnd = modBlockStart.add(count);
			ListIterator<MemoryBlock> iter = fMemoryBlockList.listIterator();

			while (iter.hasNext()) {
				MemoryBlock cachedBlock = iter.next();
				IAddress cachedBlockStart = cachedBlock.fAddress;
				IAddress cachedBlockEnd   = cachedBlock.fAddress.add(cachedBlock.fLength);
				
				// For now, we only bother to update bytes already cached.
				// Note: In a better implementation (v1.1), we would augment
				// the cache with the missing memory blocks since we went 
				// through the pains of reading them in the first place.
				// (this is left as an exercise to the reader :-)

				// Case where the modified block is completely included in the cached block  
				if (cachedBlockStart.distanceTo(modBlockStart).longValue() >= 0
					&& modBlockEnd.distanceTo(cachedBlockEnd).longValue() >= 0)
				{
					int pos = (int) cachedBlockStart.distanceTo(modBlockStart).longValue();
					System.arraycopy(modBlock, 0, cachedBlock.fBlock, pos, count);
				}
				
				// Case where the beginning of the modified block is within the cached block  
				else if (cachedBlockStart.distanceTo(modBlockStart).longValue() >= 0
					&& modBlockStart.distanceTo(cachedBlockEnd).longValue() > 0)
				{
					int pos = (int) cachedBlockStart.distanceTo(modBlockStart).longValue();
					int length = (int) cachedBlockStart.distanceTo(modBlockEnd).longValue();
					System.arraycopy(modBlock, 0, cachedBlock.fBlock, pos, length);
				}
				
				// Case where the end of the modified block is within the cached block  
				else if (cachedBlockStart.distanceTo(modBlockEnd).longValue() > 0
					&& modBlockEnd.distanceTo(cachedBlockEnd).longValue() >= 0)
				{
					int pos = (int) modBlockStart.distanceTo(cachedBlockStart).longValue();
					int length = (int) cachedBlockStart.distanceTo(modBlockEnd).longValue();
					System.arraycopy(modBlock, pos, cachedBlock.fBlock, 0, length);
				}
 			}
			return;
		}

	    /**
		 * @param memoryDMC
	     * @param address	the memory block address (on the target)
	     * @param word_size	the size, in bytes, of an addressable item
	     * @param count		the number of bytes to read
	     * @param drm		the asynchronous data request monitor
	     */
	    public void getMemory(IMemoryDMContext memoryDMC, final IAddress address, final int word_size, 
	    		final int count, final DataRequestMonitor<MemoryByte[]> drm)
	    {
	    	// Determine the number of read requests to issue 
	    	LinkedList<MemoryBlock> missingBlocks = getListOfMissingBlocks(address, count);
	    	int numberOfRequests = missingBlocks.size();

	    	// A read request will be issued for each block needed
	    	// so we need to keep track of the count
	        final CountingRequestMonitor countingRM =
	        	new CountingRequestMonitor(getExecutor(), drm) { 
	                @Override
	                protected void handleSuccess() {
	                	// We received everything so read the result from the memory cache
	                	drm.setData(getMemoryBlockFromCache(address, count));
	                    drm.done();
	                }
	            };
	       	countingRM.setDoneCount(numberOfRequests);

	        // Issue the read requests
	        for (int i = 0; i < numberOfRequests; i++) {
	        	MemoryBlock block = missingBlocks.get(i);
	        	final IAddress startAddress = block.fAddress;
	        	final int length = (int) block.fLength;
		        readMemoryBlock(memoryDMC, startAddress, 0, word_size, length,
					    new DataRequestMonitor<MemoryByte[]>(getSession().getExecutor(), drm) {
					    	@Override
					    	protected void handleSuccess() {
					    		MemoryByte[] block = new MemoryByte[count];
					    		block = getData();
					    		MemoryBlock memoryBlock = new MemoryBlock(startAddress, length, block);
					    		fMemoryBlockList.add(memoryBlock);
					    		countingRM.done();
					    	}
					    });
	        }
	    }

	    /**
		 * @param memoryDMC
	     * @param address	the memory block address (on the target)
	     * @param offset	the offset from the start address
	     * @param word_size	the size, in bytes, of an addressable item
	     * @param count		the number of bytes to write
	     * @param buffer	the source buffer
	     * @param rm		the asynchronous request monitor
	     */
	   public void setMemory(final IMemoryDMContext memoryDMC, final IAddress address,
			   final long offset, final int word_size, final int count, final byte[] buffer,
			   final RequestMonitor rm)
	   {
	       	writeMemoryBlock(
	       	    memoryDMC, address, offset, word_size, count, buffer,
				new RequestMonitor(getSession().getExecutor(), rm) {
					@Override
				    protected void handleSuccess() {
				    	// Clear the command cache (otherwise we can't guarantee
						// that the subsequent memory read will be correct) 
						fCommandCache.reset();

				    	// Re-read the modified memory block to asynchronously update of the memory cache
				        readMemoryBlock(memoryDMC, address, offset, word_size, count,
					        new DataRequestMonitor<MemoryByte[]>(getExecutor(), rm) { 
					        	@Override
	                            protected void handleSuccess() {
									updateMemoryCache(address.add(offset), count, getData());
									// Send the MemoryChangedEvent
									IAddress[] addresses = new IAddress[count];
									for (int i = 0; i < count; i++) {
										addresses[i] = address.add(offset + i);
									}
									getSession().dispatchEvent(new MemoryChangedEvent(memoryDMC, addresses), getProperties());
									// Finally...
									rm.done();
					        	}
							});
				    }
				});
	   }

 	   /**
 	    * @param memoryDMC
 	    * @param address
 	    * @param offset
 	    * @param word_size
 	    * @param count
 	    * @param rm
 	    */
	   public void refreshMemory(final IMemoryDMContext memoryDMC, final IAddress address,
 			   final long offset, final int word_size, final int count, final RequestMonitor rm)
	   {
		   // Check if we already cache part of this memory area (which means it
		   // is used by a memory service client that will have to be updated)
		   LinkedList<MemoryBlock> list = getListOfMissingBlocks(address, count);
		   int sizeToRead = 0;
		   for (MemoryBlock block : list) {
			   sizeToRead += block.fLength;
		   }

		   // If none of the requested memory is in cache, just get out
		   if (sizeToRead == count) {
			   rm.done();
			   return;
		   }
		
		   // Prepare the data for the MemoryChangedEvent
		   final IAddress[] addresses = new IAddress[count];
		   for (int i = 0; i < count; i++) {
			   addresses[i] = address.add(i);
		   }

		   // Read the corresponding memory block
		   fCommandCache.reset();
		   readMemoryBlock(memoryDMC, address, 0, 1, count,
				   new DataRequestMonitor<MemoryByte[]>(getExecutor(), rm) {
					   @Override
					   protected void handleSuccess() {
						   MemoryByte[] oldBlock = getMemoryBlockFromCache(address, count);
						   MemoryByte[] newBlock = getData();
						   boolean blocksDiffer = false;
						   for (int i = 0; i < oldBlock.length; i++) {
						       if (oldBlock[i].getValue() != newBlock[i].getValue()) {
						          blocksDiffer = true;
						          break;
						       }
						   }
						   if (blocksDiffer) {
							   updateMemoryCache(address, count, newBlock);
							   getSession().dispatchEvent(new MemoryChangedEvent(memoryDMC, addresses), getProperties());
						   }
						   rm.done();
					   }
			   });
 		}
	}

   /**
    * {@inheritDoc}
    * @since 1.1
    */
    public void flushCache(IDMContext context) {
    	fCommandCache.reset(context);
    	
    	IMemoryDMContext memoryDMC = DMContexts.getAncestorOfType(context, IMemoryDMContext.class);
    	if (fMemoryCaches.containsKey(memoryDMC)) {
    		// We do not want to use the call to getMemoryCache() here.
    		// This is because:
    		// 1- if there is not an entry already , we do not want to automatically 
    		//    create one, just to call reset() on it.
    		// 2- if memoryDMC == null, we do not want to create a cache
    		//    entry for which the key is 'null'
    		fMemoryCaches.get(memoryDMC).reset();
    	}
    }
}
