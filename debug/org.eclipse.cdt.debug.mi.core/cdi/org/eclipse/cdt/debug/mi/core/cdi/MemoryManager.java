/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MIFormat;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.model.MemoryBlock;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIDataReadMemory;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIMemoryChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIMemoryCreatedEvent;
import org.eclipse.cdt.debug.mi.core.output.MIDataReadMemoryInfo;

/**
 */
public class MemoryManager extends Manager implements ICDIMemoryManager {

	List blockList;

	public MemoryManager(Session session) {
		super(session, true);
		blockList = new ArrayList();
	}

	/**
	 * This method will be call by the eventManager.processSuspended() every time the
	 * inferior comes to a Stop/Suspended.  It will allow to look at the blocks that
	 * are registered and fired any event if changed.
	 * Note: Frozen blocks are not updated.
	 *
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager#createMemoryBlock(long, int)
	 */
	public void update() {
		Session session = (Session)getSession();
		MISession mi = session.getMISession();
		MemoryBlock[] blocks = listMemoryBlocks();
		List eventList = new ArrayList(blocks.length);
		for (int i = 0; i < blocks.length; i++) {
			if (! blocks[i].isFrozen()) {
				try {
					update(blocks[i], eventList);
				} catch (CDIException e) {
				}
			}
		}
		MIEvent[] events = (MIEvent[])eventList.toArray(new MIEvent[0]);
		mi.fireEvents(events);
	}

	/**
	 * update one Block.
	 */
	public Long[] update(MemoryBlock block, List aList) throws CDIException {
		MemoryBlock newBlock = cloneBlock(block);
		boolean newAddress = ( newBlock.getStartAddress() != block.getStartAddress() );
		Long[] array = compareBlocks(block, newBlock);
		// Update the block MIDataReadMemoryInfo.
		block.setMIDataReadMemoryInfo(newBlock.getMIDataReadMemoryInfo());
		if (array.length > 0 || newAddress) {
			if (aList != null) {
				aList.add(new MIMemoryChangedEvent(array));
			} else {
				Session session = (Session)getSession();
				MISession mi = session.getMISession();
				mi.fireEvent(new MIMemoryChangedEvent(array));
			}
		}
		return array;
	}

	/**
	 * @return the registers MemoryBlock.
	 */
	public MemoryBlock[] listMemoryBlocks() {
		return (MemoryBlock[])blockList.toArray(new MemoryBlock[0]);
	}

	/**
	 * Compare two blocks and return an array of all _addresses_ that are different.
	 * This method is not smart it always assume that:
	 * oldBlock.getStartAddress() == newBlock.getStartAddress;
	 * oldBlock.getLength() == newBlock.getLength();
	 * @return Long[] array of modified addresses.
	 */
	Long[] compareBlocks (MemoryBlock oldBlock, MemoryBlock newBlock) throws CDIException {
		byte[] oldBytes = oldBlock.getBytes();
		byte[] newBytes = newBlock.getBytes();
		List aList = new ArrayList(newBytes.length);
		long diff = newBlock.getStartAddress() - oldBlock.getStartAddress();
		if ( Math.abs( diff ) < newBytes.length ) {
			for (int i = 0; i < newBytes.length; i++) {
				if (i + (int)diff < oldBytes.length && i + (int)diff >= 0) {
					if (oldBytes[i + (int)diff] != newBytes[i]) {
						aList.add(new Long(newBlock.getStartAddress() + i));
					}
				}
			}
		}
		return (Long[])aList.toArray(new Long[0]);
	}

	/**
	 * Use the same expression and length of the original block
	 * to create a new MemoryBlock.  The new block is not register
	 * with the MemoryManager.
	 */
	MemoryBlock cloneBlock(MemoryBlock block) throws CDIException {
		Session session = (Session)getSession();
		String exp = block.getExpression();
		MIDataReadMemoryInfo info = createMIDataReadMemoryInfo(exp, (int)block.getLength());
		return new MemoryBlock(session.getCurrentTarget(), exp, info);
	}

	/**
	 * Post a -data-read-memory to gdb/mi.
	 */
	MIDataReadMemoryInfo createMIDataReadMemoryInfo(String exp, int length) throws CDIException {
		Session session = (Session)getSession();
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIDataReadMemory mem = factory.createMIDataReadMemory(0, exp, MIFormat.HEXADECIMAL, 1, 1, length, null);
		try {
			mi.postCommand(mem);
			MIDataReadMemoryInfo info = mem.getMIDataReadMemoryInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			return info;
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager#createMemoryBlock(long, int)
	 */
	public ICDIMemoryBlock createMemoryBlock(long address, int length)
		throws CDIException {
		String addr = "0x" + Long.toHexString(address); //$NON-NLS-1$
		return createMemoryBlock(addr, length);
	}
		
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager#createMemoryBlock(string, int)
	 */
	public ICDIMemoryBlock createMemoryBlock(String address, int length) throws CDIException {
		Session session = (Session)getSession();
		MIDataReadMemoryInfo info = createMIDataReadMemoryInfo(address, length);
		ICDIMemoryBlock block = new MemoryBlock(session.getCurrentTarget(), address, info);
		blockList.add(block);
		MISession mi = session.getMISession();
		mi.fireEvent(new MIMemoryCreatedEvent(block.getStartAddress(), block.getLength()));
		return block;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager#getBlocks()
	 */
	public ICDIMemoryBlock[] getMemoryBlocks() throws CDIException {
		return listMemoryBlocks();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager#removeAllBlocks()
	 */
	public void removeAllBlocks() throws CDIException {
		blockList.clear();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager#removeBlock(ICDIMemoryBlock)
	 */
	public void removeBlock(ICDIMemoryBlock memoryBlock) throws CDIException {
		blockList.remove(memoryBlock);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager#removeBlocks(ICDIMemoryBlock[])
	 */
	public void removeBlocks(ICDIMemoryBlock[] memoryBlocks)
		throws CDIException {
		for (int i = 0; i < memoryBlocks.length; i++) {
			removeBlock(memoryBlocks[i]);
		}
	}

}
