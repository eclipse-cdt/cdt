/*******************************************************************************
 * Copyright (c) 2007, 2015 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson AB - expanded from initial stub
 *     Ericsson AB - added support for event handling
 *     Ericsson AB - added memory cache
 *     Vladimir Prus (CodeSourcery) - support for -data-read-memory-bytes (bug 322658)
 *     John Dallaway - support for -data-write-memory-bytes (bug 387793)
 *     John Dallaway - memory cache update fix (bug 387688)
 *     Alvaro Sanchez-Leon (Ericsson AB) - [Memory] Support 16 bit addressable size (Bug 426730)
 *     Alvaro Sanchez-Leon (Ericsson AB) - [Memory] Memory space cache not being reset (Bug 432963)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionChangedDMEvent;
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
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
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
	//data-read-memory write is deprecated, its description could be ambiguous for e.g. 16 bit addressable systems
	private static final String DATA_WRITE_MEMORY_16_NOT_SUPPORTED = "data-write-memory with word-size != 1 not supported"; //$NON-NLS-1$

	public class MemoryChangedEvent extends AbstractDMEvent<IMemoryDMContext> implements IMemoryChangedEvent {
		private IAddress[] fAddresses;

		public MemoryChangedEvent(IMemoryDMContext context, IAddress[] addresses) {
			super(context);
			fAddresses = addresses;
		}

		@Override
		public IAddress[] getAddresses() {
			return fAddresses;
		}
	}

	// Back-end commands cache
	private CommandCache fCommandCache;
	private CommandFactory fCommandFactory;

	// Map of memory caches
	private Map<IMemoryDMContext, MIMemoryCache> fMemoryCaches;

	/** @since 4.2 */
	protected MIMemoryCache getMemoryCache(IMemoryDMContext memoryDMC) {
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
	 */
	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new ImmediateRequestMonitor(requestMonitor) {
			@Override
			protected void handleSuccess() {
				doInitialize(requestMonitor);
			}
		});
	}

	private void doInitialize(final RequestMonitor requestMonitor) {
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

		register(new String[] { MIMemory.class.getName(), IMemory.class.getName() }, new Hashtable<String, String>());

		fMemoryCaches = new HashMap<>();

		getSession().addServiceEventListener(this, null);

		requestMonitor.done();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.service.AbstractDsfService#shutdown(org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	@Override
	public void shutdown(final RequestMonitor requestMonitor) {

		unregister();

		getSession().removeServiceEventListener(this);

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
	@Override
	public void getMemory(IMemoryDMContext memoryDMC, IAddress address, long offset, int wordSize, int wordCount,
			DataRequestMonitor<MemoryByte[]> drm) {
		if (memoryDMC == null) {
			drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Unknown context type", null)); //$NON-NLS-1$);
			drm.done();
			return;
		}

		if (wordSize < 1) {
			drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Word size not supported (< 1)", //$NON-NLS-1$
					null));
			drm.done();
			return;
		}

		if (wordCount < 0) {
			drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
					"Invalid word count (< 0)", null)); //$NON-NLS-1$
			drm.done();
			return;
		}

		getMemoryCache(memoryDMC).getMemory(memoryDMC, address.add(offset), wordSize, wordCount, drm);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IMemory#setMemory(org.eclipse.cdt.dsf.datamodel.IDMContext, org.eclipse.cdt.core.IAddress, long, int, byte[], org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	@Override
	public void setMemory(IMemoryDMContext memoryDMC, IAddress address, long offset, int wordSize, int wordCount,
			byte[] buffer, RequestMonitor rm) {
		if (memoryDMC == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Unknown context type", null)); //$NON-NLS-1$);
			rm.done();
			return;
		}

		if (wordSize < 1) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Word size not supported (< 1)", //$NON-NLS-1$
					null));
			rm.done();
			return;
		}

		if (wordCount < 0) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
					"Invalid word count (< 0)", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		if (buffer.length < wordCount * wordSize) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
					"Buffer too short", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		getMemoryCache(memoryDMC).setMemory(memoryDMC, address, offset, wordSize, wordCount, buffer, rm);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IMemory#fillMemory(org.eclipse.cdt.dsf.datamodel.IDMContext, org.eclipse.cdt.core.IAddress, long, int, byte[], org.eclipse.cdt.dsf.concurrent.RequestMonitor)
	 */
	@Override
	public void fillMemory(IMemoryDMContext memoryDMC, IAddress address, long offset, int wordSize, int count,
			byte[] pattern, RequestMonitor rm) {
		if (memoryDMC == null) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INTERNAL_ERROR, "Unknown context type", null)); //$NON-NLS-1$);
			rm.done();
			return;
		}

		if (wordSize < 1) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Word size not supported (< 1)", //$NON-NLS-1$
					null));
			rm.done();
			return;
		}

		if (count < 0) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
					"Invalid repeat count (< 0)", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		if (pattern.length < 1) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
					"Empty pattern", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		// Create an aggregate buffer so we can write in 1 shot
		int length = pattern.length;
		byte[] buffer = new byte[count * length];
		for (int i = 0; i < count; i++) {
			System.arraycopy(pattern, 0, buffer, i * length, length);
		}

		int word_count = buffer.length / wordSize;
		if (buffer.length % wordSize != 0) {
			word_count++;
		}

		// All is clear: go for it
		getMemoryCache(memoryDMC).setMemory(memoryDMC, address, offset, wordSize, word_count, buffer, rm);
	}

	///////////////////////////////////////////////////////////////////////
	// Back-end functions
	///////////////////////////////////////////////////////////////////////

	/**
	 * @param memoryDMC
	 * @param address
	 * @param offset
	 * @param wordSize
	 * @param wordCount in addressable units
	 * @param drm
	 *
	 * @since 1.1
	 */
	protected void readMemoryBlock(IDMContext dmc, IAddress address, final long offset, final int wordSize,
			final int wordCount, final DataRequestMonitor<MemoryByte[]> drm) {
		if (fDataReadMemoryBytes) {
			fCommandCache.execute(
					fCommandFactory.createMIDataReadMemoryBytes(dmc, address.toString(), offset, wordCount, wordSize),
					new DataRequestMonitor<MIDataReadMemoryBytesInfo>(getExecutor(), drm) {
						@Override
						protected void handleSuccess() {
							// Retrieve the memory block
							drm.setData(getData().getMIMemoryBlock());
							drm.done();
						}

						@Override
						protected void handleFailure() {
							drm.setData(createInvalidBlock(wordSize * wordCount));
							drm.done();
						}
					});
		} else {
			if (wordSize != 1) {
				//The word-size is specified within the resulting command data-read-memory
				//The word-size is defined in bytes although in the MI interface it's not clear if the meaning is
				//octets or system dependent bytes (minimum addressable memory).
				//As this command is deprecated there is no good reason to augment the support for word sizes != 1
				drm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED,
						DATA_WRITE_MEMORY_16_NOT_SUPPORTED, null));
				drm.done();
				return;
			}

			/* To simplify the parsing of the MI result, we request the output to
			 * be on 1 row of [count] columns, no char interpretation.
			 */
			int mode = MIFormat.HEXADECIMAL;
			int nbRows = 1;
			int nbCols = wordCount;
			Character asChar = null;

			fCommandCache
					.execute(
							fCommandFactory.createMIDataReadMemory(dmc, offset, address.toString(), mode, wordSize,
									nbRows, nbCols, asChar),
							new DataRequestMonitor<MIDataReadMemoryInfo>(getExecutor(), drm) {
								@Override
								protected void handleSuccess() {
									// Retrieve the memory block
									drm.setData(getData().getMIMemoryBlock());
									drm.done();
								}

								@Override
								protected void handleFailure() {
									drm.setData(createInvalidBlock(wordSize * wordCount));
									drm.done();
								}
							});
		}
	}

	private MemoryByte[] createInvalidBlock(int size) {
		// Bug234289: If memory read fails, return a block marked as invalid
		MemoryByte[] block = new MemoryByte[size];
		for (int i = 0; i < block.length; i++) {
			block[i] = new MemoryByte((byte) 0, (byte) 0);
		}
		return block;
	}

	/**
	 * @param memoryDMC
	 * @param address
	 * @param offset
	 * @param wordSize
	 * @param wordCount in addressable units
	 * @param buffer
	 * @param rm
	 *
	 * @since 1.1
	 */
	protected void writeMemoryBlock(final IDMContext dmc, final IAddress address, final long offset, final int wordSize,
			final int wordCount, final byte[] buffer, final RequestMonitor rm) {
		if (fDataReadMemoryBytes) {
			// Use -data-write-memory-bytes for performance,
			fCommandCache.execute(
					fCommandFactory.createMIDataWriteMemoryBytes(dmc, address.add(offset).toString(),
							(buffer.length == wordCount * wordSize) ? buffer
									: Arrays.copyOf(buffer, wordCount * wordSize)),
					new DataRequestMonitor<MIInfo>(getExecutor(), rm));
		} else {
			if (wordSize != 1) {
				//The word-size is specified within the resulting command data-write-memory
				//The word-size is defined in bytes although in the MI interface it's not clear if the meaning is
				//octets or system dependent bytes (minimum addressable memory).
				//As this command is deprecated there is no good reason to augment the support for word sizes != 1
				rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, REQUEST_FAILED,
						DATA_WRITE_MEMORY_16_NOT_SUPPORTED, null));
				rm.done();
				return;
			}

			// Each byte is written individually (GDB power...)
			// so we need to keep track of the count
			final CountingRequestMonitor countingRM = new CountingRequestMonitor(getExecutor(), rm);
			countingRM.setDoneCount(wordCount);

			// We will format the individual bytes in decimal
			int format = MIFormat.DECIMAL;
			String baseAddress = address.toString();

			// Issue an MI request for each byte to write
			for (int i = 0; i < wordCount; i++) {
				String value = Byte.toString(buffer[i]);
				fCommandCache.execute(
						fCommandFactory.createMIDataWriteMemory(dmc, offset + i, baseAddress, format, wordSize, value),
						new DataRequestMonitor<MIDataWriteMemoryInfo>(getExecutor(), countingRM));
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Event handlers
	//////////////////////////////////////////////////////////////////////////
	@DsfServiceEventHandler
	public void eventDispatched(IResumedDMEvent e) {
		if (e instanceof IContainerResumedDMEvent) {
			fCommandCache.setContextAvailable(e.getDMContext(), false);
		}

		if (e.getReason() != StateChangeReason.STEP) {
			IMemoryDMContext memoryDMC = DMContexts.getAncestorOfType(e.getDMContext(), IMemoryDMContext.class);
			// It is the memory context we want to clear, not only the context that resumed.  The resumed context
			// is probably a thread but that running thread could have changed any memory within the memory
			// context.
			if (memoryDMC != null) {
				fCommandCache.reset(memoryDMC);

				memoryCacheReset(memoryDMC);
			}
		}
	}

	@DsfServiceEventHandler
	public void eventDispatched(ISuspendedDMEvent e) {
		if (e instanceof IContainerSuspendedDMEvent) {
			fCommandCache.setContextAvailable(e.getDMContext(), true);
		}

		IMemoryDMContext memoryDMC = DMContexts.getAncestorOfType(e.getDMContext(), IMemoryDMContext.class);
		// It is the memory context we want to clear, not only the context that stopped.  The stopped context
		// is probably a thread but that thread that ran could have changed any memory within the memory
		// context.
		if (memoryDMC != null) {
			fCommandCache.reset(memoryDMC);

			memoryCacheReset(memoryDMC);
		}
	}

	/**
	 * @deprecated Replaced by the generic {@link #eventDispatched(IExpressionChangedDMEvent)}
	 */
	@Deprecated
	public void eventDispatched(ExpressionChangedEvent e) {
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 4.2
	 */
	@DsfServiceEventHandler
	public void eventDispatched(IExpressionChangedDMEvent e) {

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
							IAddress expAddress = expression.getAddress();
							if (expAddress != IExpressions.IExpressionDMLocation.INVALID_ADDRESS) {
								final int count = expression.getSize();
								final Addr64 address;
								if (expAddress instanceof Addr64) {
									address = (Addr64) expAddress;
								} else {
									address = new Addr64(expAddress.getValue());
								}

								final IMemoryDMContext memoryDMC = DMContexts.getAncestorOfType(context,
										IMemoryDMContext.class);
								getMemoryCache(memoryDMC).refreshMemory(memoryDMC, address, 0,
										getAddressableSize(memoryDMC), count, true,
										new RequestMonitor(getExecutor(), null));
							}
						}
					});
		}
	}

	/**
	 * The default addressable size is set to 1 octet, to be overridden by sub-classes supporting different values
	 * @since 4.4
	 */
	protected int getAddressableSize(IMemoryDMContext context) {
		return 1;
	}

	///////////////////////////////////////////////////////////////////////////
	// SortedLinkedlist
	///////////////////////////////////////////////////////////////////////////

	// This class is really the equivalent of a C struct (old habits die hard...)
	// For simplicity, everything is public.
	private static class MemoryBlock {
		public IAddress fAddress;
		public long fLengthInAddressableUnits;
		public long fLengthInOctets;
		public MemoryByte[] fBlock;

		public MemoryBlock(IAddress address, long lengthInOctets, long lengthInAddressableUnits, MemoryByte[] block) {
			// A memory block is expected to be populated with the contents of a defined range of addresses
			// therefore the number of octets shall be divisible by the number of addresses
			assert (lengthInOctets % lengthInAddressableUnits == 0);
			fAddress = address;
			fLengthInAddressableUnits = lengthInAddressableUnits;
			fLengthInOctets = lengthInOctets;
			fBlock = block;
		}
	}

	// Address-ordered data structure to cache the memory blocks.
	// Contiguous blocks are merged if possible.
	@SuppressWarnings("serial")
	private static class SortedMemoryBlockList extends LinkedList<MemoryBlock> {

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
				IAddress endOfPreviousBlock = prevBlock.fAddress.add(prevBlock.fLengthInAddressableUnits);
				if (endOfPreviousBlock.distanceTo(newBlock.fAddress).longValue() == 0) {
					long newLengthInOctets = prevBlock.fLengthInOctets + newBlock.fLengthInOctets;
					long newLengthInAddressableUnits = prevBlock.fLengthInAddressableUnits
							+ newBlock.fLengthInAddressableUnits;
					if (newLengthInOctets <= Integer.MAX_VALUE) {
						MemoryByte[] block = new MemoryByte[(int) newLengthInOctets];
						System.arraycopy(prevBlock.fBlock, 0, block, 0, (int) prevBlock.fLengthInOctets);
						System.arraycopy(newBlock.fBlock, 0, block, (int) prevBlock.fLengthInOctets,
								(int) newBlock.fLengthInOctets);
						newBlock = new MemoryBlock(prevBlock.fAddress, newLengthInOctets, newLengthInAddressableUnits,
								block);
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
				IAddress endOfNewBlock = newBlock.fAddress.add(newBlock.fLengthInAddressableUnits);
				if (endOfNewBlock.distanceTo(nextBlock.fAddress).longValue() == 0) {
					long newLength = newBlock.fLengthInOctets + nextBlock.fLengthInOctets;
					long newAddressesLength = newBlock.fLengthInAddressableUnits + nextBlock.fLengthInAddressableUnits;
					if (newLength <= Integer.MAX_VALUE) {
						MemoryByte[] block = new MemoryByte[(int) newLength];
						System.arraycopy(newBlock.fBlock, 0, block, 0, (int) newBlock.fLengthInOctets);
						System.arraycopy(nextBlock.fBlock, 0, block, (int) newBlock.fLengthInOctets,
								(int) nextBlock.fLengthInOctets);
						newBlock = new MemoryBlock(newBlock.fAddress, newLength, newAddressesLength, block);
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

	/** @since 4.2 */
	protected class MIMemoryCache {
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
		private List<MemoryBlock> getListOfMissingBlocks(IAddress reqBlockStart, int wordCount, int wordSize) {
			int octetCount = wordCount * wordSize;

			LinkedList<MemoryBlock> list = new LinkedList<>();
			ListIterator<MemoryBlock> it = fMemoryBlockList.listIterator();

			// Look for holes in the list of memory blocks
			while (it.hasNext() && octetCount > 0) {
				MemoryBlock cachedBlock = it.next();
				IAddress cachedBlockStart = cachedBlock.fAddress;
				IAddress cachedBlockEnd = cachedBlock.fAddress.add(cachedBlock.fLengthInAddressableUnits);

				// Case where we miss a block before the cached block
				if (reqBlockStart.distanceTo(cachedBlockStart).longValue() >= 0) {
					int lengthInOctets = (int) Math
							.min(reqBlockStart.distanceTo(cachedBlockStart).longValue() * wordSize, octetCount);
					// If both blocks start at the same location, no need to create a new cached block
					if (lengthInOctets > 0) {
						int lengthInAddressableUnits = lengthInOctets / wordSize;
						MemoryBlock newBlock = new MemoryBlock(reqBlockStart, lengthInOctets, lengthInAddressableUnits,
								new MemoryByte[0]);
						list.add(newBlock);
					}
					// Adjust request block start and length for the next iteration
					reqBlockStart = cachedBlockEnd;
					octetCount -= lengthInOctets + cachedBlock.fLengthInOctets;
				}

				// Case where the requested block starts somewhere in the cached block
				else if (cachedBlockStart.distanceTo(reqBlockStart).longValue() > 0
						&& reqBlockStart.distanceTo(cachedBlockEnd).longValue() >= 0) {
					// Start of the requested block already in cache
					// Adjust request block start and length for the next iteration
					octetCount -= reqBlockStart.distanceTo(cachedBlockEnd).longValue() * wordSize;
					reqBlockStart = cachedBlockEnd;
				}
			}

			// Case where we miss a block at the end of the cache
			if (octetCount > 0) {
				int addressesLength = octetCount / wordSize;
				MemoryBlock newBlock = new MemoryBlock(reqBlockStart, octetCount, addressesLength, new MemoryByte[0]);
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
		private MemoryByte[] getMemoryBlockFromCache(IAddress reqBlockStart, int wordCount, int wordSize) {
			int count = wordCount * wordSize;

			IAddress reqBlockEnd = reqBlockStart.add(wordCount);
			MemoryByte[] resultBlock = new MemoryByte[count];
			ListIterator<MemoryBlock> iter = fMemoryBlockList.listIterator();

			while (iter.hasNext()) {
				MemoryBlock cachedBlock = iter.next();
				IAddress cachedBlockStart = cachedBlock.fAddress;
				IAddress cachedBlockEnd = cachedBlock.fAddress.add(cachedBlock.fLengthInAddressableUnits);

				// Case where the cached block overlaps completely the requested memory block
				if (cachedBlockStart.distanceTo(reqBlockStart).longValue() >= 0
						&& reqBlockEnd.distanceTo(cachedBlockEnd).longValue() >= 0) {
					int pos = (int) cachedBlockStart.distanceTo(reqBlockStart).longValue() * wordSize;
					System.arraycopy(cachedBlock.fBlock, pos, resultBlock, 0, count);
				}

				// Case where the beginning of the cached block is within the requested memory block
				else if (reqBlockStart.distanceTo(cachedBlockStart).longValue() >= 0
						&& cachedBlockStart.distanceTo(reqBlockEnd).longValue() > 0) {
					int pos = (int) reqBlockStart.distanceTo(cachedBlockStart).longValue() * wordSize;
					int length = (int) Math.min(cachedBlock.fLengthInOctets, count - pos);
					System.arraycopy(cachedBlock.fBlock, 0, resultBlock, pos, length);
				}

				// Case where the end of the cached block is within the requested memory block
				else if (cachedBlockStart.distanceTo(reqBlockStart).longValue() >= 0
						&& reqBlockStart.distanceTo(cachedBlockEnd).longValue() > 0) {
					int pos = (int) cachedBlockStart.distanceTo(reqBlockStart).longValue() * wordSize;
					int length = (int) Math.min(cachedBlock.fLengthInOctets - pos, count);
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
		 * @param wordCount - Number of addressable units
		 * @param modBlock
		 * @param wordSize - Number of octets per addressable unit
		 */
		private void updateMemoryCache(IAddress modBlockStart, int wordCount, MemoryByte[] modBlock, int wordSize) {
			IAddress modBlockEnd = modBlockStart.add(wordCount);
			ListIterator<MemoryBlock> iter = fMemoryBlockList.listIterator();
			int count = wordCount * wordSize;

			while (iter.hasNext()) {
				MemoryBlock cachedBlock = iter.next();
				IAddress cachedBlockStart = cachedBlock.fAddress;
				IAddress cachedBlockEnd = cachedBlock.fAddress.add(cachedBlock.fLengthInAddressableUnits);

				// For now, we only bother to update bytes already cached.
				// Note: In a better implementation (v1.1), we would augment
				// the cache with the missing memory blocks since we went
				// through the pains of reading them in the first place.
				// (this is left as an exercise to the reader :-)

				// Case where the modified block is completely included in the cached block
				if (cachedBlockStart.distanceTo(modBlockStart).longValue() >= 0
						&& modBlockEnd.distanceTo(cachedBlockEnd).longValue() >= 0) {
					int pos = (int) cachedBlockStart.distanceTo(modBlockStart).longValue() * wordSize;
					System.arraycopy(modBlock, 0, cachedBlock.fBlock, pos, count);
				}

				// Case where the cached block is completely included in the modified block
				else if (modBlockStart.distanceTo(cachedBlockStart).longValue() >= 0
						&& cachedBlockEnd.distanceTo(modBlockEnd).longValue() >= 0) {
					int pos = (int) modBlockStart.distanceTo(cachedBlockStart).longValue() * wordSize;
					System.arraycopy(modBlock, pos, cachedBlock.fBlock, 0, (int) cachedBlock.fLengthInOctets);
				}

				// Case where the beginning of the modified block is within the cached block
				else if (cachedBlockStart.distanceTo(modBlockStart).longValue() >= 0
						&& modBlockStart.distanceTo(cachedBlockEnd).longValue() > 0) {
					int pos = (int) cachedBlockStart.distanceTo(modBlockStart).longValue() * wordSize;
					int length = (int) modBlockStart.distanceTo(cachedBlockEnd).longValue() * wordSize;
					System.arraycopy(modBlock, 0, cachedBlock.fBlock, pos, length);
				}

				// Case where the end of the modified block is within the cached block
				else if (cachedBlockStart.distanceTo(modBlockEnd).longValue() > 0
						&& modBlockEnd.distanceTo(cachedBlockEnd).longValue() >= 0) {
					int pos = (int) modBlockStart.distanceTo(cachedBlockStart).longValue() * wordSize;
					int length = (int) cachedBlockStart.distanceTo(modBlockEnd).longValue() * wordSize;
					System.arraycopy(modBlock, pos, cachedBlock.fBlock, 0, length);
				}
			}
			return;
		}

		/**
		 * @param memoryDMC
		 * @param address	the memory block address (on the target)
		 * @param wordSize	the size, in bytes, of an addressable item
		 * @param wordCount the number of addressable units to read
		 * @param drm		the asynchronous data request monitor
		 */
		public void getMemory(IMemoryDMContext memoryDMC, final IAddress address, final int wordSize,
				final int wordCount, final DataRequestMonitor<MemoryByte[]> drm) {
			// Determine the number of read requests to issue
			List<MemoryBlock> missingBlocks = getListOfMissingBlocks(address, wordCount, wordSize);
			int numberOfRequests = missingBlocks.size();

			// A read request will be issued for each block needed
			// so we need to keep track of the count
			final CountingRequestMonitor countingRM = new CountingRequestMonitor(getExecutor(), drm) {
				@Override
				protected void handleSuccess() {
					// We received everything so read the result from the memory cache
					drm.setData(getMemoryBlockFromCache(address, wordCount, wordSize));
					drm.done();
				}
			};
			countingRM.setDoneCount(numberOfRequests);

			// Issue the read requests
			for (int i = 0; i < numberOfRequests; i++) {
				MemoryBlock block = missingBlocks.get(i);
				final IAddress startAddress = block.fAddress;
				final int length = (int) block.fLengthInAddressableUnits;
				readMemoryBlock(memoryDMC, startAddress, 0, wordSize, length,
						new DataRequestMonitor<MemoryByte[]>(getSession().getExecutor(), drm) {
							@Override
							protected void handleSuccess() {
								MemoryByte[] block = getData();
								int lenghtInaddressableUnits = block.length / wordSize;
								MemoryBlock memoryBlock = new MemoryBlock(startAddress, block.length,
										lenghtInaddressableUnits, block);
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
		 * @param wordSize	the size, in bytes, of an addressable item
		 * @param wordCount the number of addressable units to write
		 * @param buffer	the source buffer
		 * @param rm		the asynchronous request monitor
		 */
		public void setMemory(final IMemoryDMContext memoryDMC, final IAddress address, final long offset,
				final int wordSize, final int wordCount, final byte[] buffer, final RequestMonitor rm) {
			writeMemoryBlock(memoryDMC, address, offset, wordSize, wordCount, buffer,
					new RequestMonitor(getSession().getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							// Clear the command cache (otherwise we can't guarantee
							// that the subsequent memory read will be correct)
							fCommandCache.reset();

							// Re-read the modified memory block to asynchronously update of the memory cache
							readMemoryBlock(memoryDMC, address, offset, wordSize, wordCount,
									new DataRequestMonitor<MemoryByte[]>(getExecutor(), rm) {
										@Override
										protected void handleSuccess() {
											updateMemoryCache(address.add(offset), wordCount, getData(), wordSize);
											// Send the MemoryChangedEvent
											IAddress[] addresses = new IAddress[wordCount];
											for (int i = 0; i < wordCount; i++) {
												addresses[i] = address.add(offset + i);
											}
											getSession().dispatchEvent(new MemoryChangedEvent(memoryDMC, addresses),
													getProperties());
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
		* @param wordSize
		* @param wordCount
		* @param sendMemoryEvent Indicates if a IMemoryChangedEvent should be sent if the memory cache has changed.
		* @param rm
		*/
		public void refreshMemory(final IMemoryDMContext memoryDMC, final IAddress address, final long offset,
				final int wordSize, final int wordCount, final boolean sendMemoryEvent, final RequestMonitor rm) {
			// Check if we already cache part of this memory area (which means it
			// is used by a memory service client that will have to be updated)
			List<MemoryBlock> list = getListOfMissingBlocks(address, wordCount, wordSize);
			int sizeToRead = 0;
			for (MemoryBlock block : list) {
				sizeToRead += block.fLengthInAddressableUnits;
			}

			// If none of the requested memory is in cache, just get out
			if (sizeToRead == wordCount) {
				rm.done();
				return;
			}

			// Read the corresponding memory block
			fCommandCache.reset();
			readMemoryBlock(memoryDMC, address, offset, wordSize, wordCount,
					new DataRequestMonitor<MemoryByte[]>(getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							MemoryByte[] oldBlock = getMemoryBlockFromCache(address, wordCount, wordSize);
							MemoryByte[] newBlock = getData();
							boolean blocksDiffer = false;
							for (int i = 0; i < oldBlock.length; i++) {
								if (oldBlock[i].getValue() != newBlock[i].getValue()) {
									blocksDiffer = true;
									break;
								}
							}
							if (blocksDiffer) {
								updateMemoryCache(address.add(offset), wordCount, newBlock, wordSize);
								if (sendMemoryEvent) {
									// Send the MemoryChangedEvent
									final IAddress[] addresses = new IAddress[wordCount];
									for (int i = 0; i < wordCount; i++) {
										addresses[i] = address.add(offset + i);
									}
									getSession().dispatchEvent(new MemoryChangedEvent(memoryDMC, addresses),
											getProperties());
								}
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
	@Override
	public void flushCache(IDMContext context) {
		fCommandCache.reset(context);

		IMemoryDMContext memoryDMC = DMContexts.getAncestorOfType(context, IMemoryDMContext.class);

		if (memoryDMC != null) {
			memoryCacheReset(memoryDMC);
		}
	}

	/**
	 * Reset the cache for the given memory context or any of its associated
	 * child memory space contexts (see Bug 432963)
	 */
	private void memoryCacheReset(IMemoryDMContext memoryDMC) {
		for (IMemoryDMContext ctx : fMemoryCaches.keySet()) {
			if (ctx != null && ctx.equals(memoryDMC) || DMContexts.isAncestorOf(ctx, memoryDMC)) {
				fMemoryCaches.get(ctx).reset();
			}
		}
	}
}
