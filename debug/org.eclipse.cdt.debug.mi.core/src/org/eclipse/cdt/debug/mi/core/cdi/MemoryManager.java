/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MIFormat;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIDataReadMemory;
import org.eclipse.cdt.debug.mi.core.output.MIDataReadMemoryInfo;

/**
 */
public class MemoryManager extends SessionObject implements ICDIMemoryManager {

	List blockList;

	public MemoryManager(CSession session) {
		super(session);
		blockList = new ArrayList();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager#createMemoryBlock(long, int)
	 */
	public ICDIMemoryBlock createMemoryBlock(long address, int length)
		throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		String addr = "0x" + Long.toHexString(address);
		MIDataReadMemory mem = factory.createMIDataReadMemory(0, addr, MIFormat.HEXADECIMAL, 1, 1, length, null);
		try {
			mi.postCommand(mem);
			MIDataReadMemoryInfo info = mem.getMIDataReadMemoryInfo();
			MemoryBlock block = new MemoryBlock(getCSession().getCTarget(), info);
			blockList.add(block);
			return block;
		} catch (MIException e) {
			throw new CDIException(e.getMessage());
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager#getBlocks()
	 */
	public ICDIMemoryBlock[] getMemoryBlocks() throws CDIException {
		return (ICDIMemoryBlock[])blockList.toArray(new ICDIMemoryBlock[0]);
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
