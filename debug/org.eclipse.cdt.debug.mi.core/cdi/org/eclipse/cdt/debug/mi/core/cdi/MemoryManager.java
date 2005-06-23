/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MIFormat;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.model.MemoryBlock;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIDataReadMemory;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MIMemoryChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIMemoryCreatedEvent;
import org.eclipse.cdt.debug.mi.core.output.MIDataReadMemoryInfo;


/**
 */
public class MemoryManager extends Manager {

	ICDIMemoryBlock[] EMPTY_MEMORY_BLOCKS = {};
	Map blockMap;

	public MemoryManager(Session session) {
		super(session, true);
		blockMap = new Hashtable();
	}

	synchronized List getMemoryBlockList(Target target) {
		List blockList = (List)blockMap.get(target);
		if (blockList == null) {
			blockList = Collections.synchronizedList(new ArrayList());
			blockMap.put(target, blockList);
		}
		return blockList;
	}

	/**
	 * This method will be call by the eventManager.processSuspended() every time the
	 * inferior comes to a Stop/Suspended.  It will allow to look at the blocks that
	 * are registered and fired any event if changed.
	 * Note: Frozen blocks are not updated.
	 *
	 */
	public void update(Target target) {
		MISession miSession = target.getMISession();
		List blockList = getMemoryBlockList(target);
		MemoryBlock[] blocks = (MemoryBlock[]) blockList.toArray(new MemoryBlock[blockList.size()]);
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
		miSession.fireEvents(events);
	}

	/**
	 * update one Block.
	 */
	public BigInteger[] update(MemoryBlock block, List aList) throws CDIException {
		Target target = (Target)block.getTarget();
		MISession miSession = target.getMISession();
		MemoryBlock newBlock = cloneBlock(block);
		boolean newAddress = ! newBlock.getStartAddress().equals(block.getStartAddress());
		BigInteger[] array = compareBlocks(block, newBlock);
		// Update the block MIDataReadMemoryInfo.
		block.setMIDataReadMemoryInfo(newBlock.getMIDataReadMemoryInfo());
		if (array.length > 0 || newAddress) {
			if (aList != null) {
				aList.add(new MIMemoryChangedEvent(miSession, array));
			} else {
				// fire right away.
				miSession.fireEvent(new MIMemoryChangedEvent(miSession, array));
			}
		}
		return array;
	}

	/**
	 * Compare two blocks and return an array of all _addresses_ that are different.
	 * This method is not smart it always assume that:
	 * oldBlock.getStartAddress() == newBlock.getStartAddress;
	 * oldBlock.getLength() == newBlock.getLength();
	 * @return Long[] array of modified addresses.
	 */
	BigInteger[] compareBlocks (MemoryBlock oldBlock, MemoryBlock newBlock) throws CDIException {
		byte[] oldBytes = oldBlock.getBytes();
		byte[] newBytes = newBlock.getBytes();
		List aList = new ArrayList(newBytes.length);
		BigInteger distance = newBlock.getStartAddress().subtract(oldBlock.getStartAddress());
		//IPF_TODO enshure it is OK here
		int diff = distance.intValue();
		if ( Math.abs(diff) <  newBytes.length) {
			for (int i = 0; i < newBytes.length; i++) {
				if (i + diff < oldBytes.length && i + diff >= 0) {
					if (oldBytes[i + diff] != newBytes[i]) {
						aList.add(newBlock.getStartAddress().add(BigInteger.valueOf(i)));
					}
				}
			}
		}
		return (BigInteger[]) aList.toArray(new BigInteger[aList.size()]);
	}

	/**
	 * Use the same expression and length of the original block
	 * to create a new MemoryBlock.  The new block is not register
	 * with the MemoryManager.
	 */
	MemoryBlock cloneBlock(MemoryBlock block) throws CDIException {
		Target target = (Target)block.getTarget();
		String exp = block.getExpression();
		int wordSize = block.getWordSize();
		boolean little = target.isLittleEndian();
		MIDataReadMemoryInfo info = createMIDataReadMemoryInfo(target.getMISession(), exp, (int)block.getLength(), wordSize);
		return new MemoryBlock(target, exp, wordSize, little, info);
	}

	/**
	 * Post a -data-read-memory to gdb/mi.
	 */
	MIDataReadMemoryInfo createMIDataReadMemoryInfo(MISession miSession, String exp, int units, int wordSize) throws CDIException {
		CommandFactory factory = miSession.getCommandFactory();
		MIDataReadMemory mem = factory.createMIDataReadMemory(0, exp, MIFormat.HEXADECIMAL, wordSize, 1, units, null);
		try {
			miSession.postCommand(mem);
			MIDataReadMemoryInfo info = mem.getMIDataReadMemoryInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			return info;
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	public ICDIMemoryBlock createMemoryBlock(Target target, String address, int units, int wordSize) throws CDIException {
		boolean little = target.isLittleEndian();
		MIDataReadMemoryInfo info = createMIDataReadMemoryInfo(target.getMISession(), address, units, wordSize);
		ICDIMemoryBlock block = new MemoryBlock(target, address, wordSize, little, info);
		List blockList = getMemoryBlockList(target);
		blockList.add(block);
		MISession miSession = target.getMISession();
		miSession.fireEvent(new MIMemoryCreatedEvent(miSession, block.getStartAddress(), block.getLength()));
		return block;
	}

	public MemoryBlock[] getMemoryBlocks(MISession miSession) {
		Session session = (Session)getSession();
		Target target = session.getTarget(miSession);
		List blockList = getMemoryBlockList(target);
		return (MemoryBlock[]) blockList.toArray(new MemoryBlock[blockList.size()]);
	}
	public ICDIMemoryBlock[] getMemoryBlocks(Target target) throws CDIException {
		List blockList = getMemoryBlockList(target);
		return (ICDIMemoryBlock[]) blockList.toArray(new ICDIMemoryBlock[blockList.size()]);
	}

	public void removeAllBlocks(Target target) throws CDIException {
		ICDIMemoryBlock[] blocks = getMemoryBlocks(target);
		removeBlocks(target, blocks);
	}

	public void removeBlocks(Target target, ICDIMemoryBlock[] memoryBlocks) throws CDIException {
		List blockList = (List)blockMap.get(target);
		if (blockList != null) {
			blockList.removeAll(Arrays.asList(memoryBlocks));
		}
	}
}
