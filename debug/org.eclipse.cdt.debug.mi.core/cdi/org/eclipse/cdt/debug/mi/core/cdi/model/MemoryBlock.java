/**********************************************************************
 * Copyright (c) 2002, 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.debug.mi.core.cdi.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MIFormat;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.CdiResources;
import org.eclipse.cdt.debug.mi.core.cdi.MI2CDIException;
import org.eclipse.cdt.debug.mi.core.cdi.MemoryManager;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIDataWriteMemory;
import org.eclipse.cdt.debug.mi.core.output.MIDataReadMemoryInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIMemory;

/**
 */
public class MemoryBlock extends CObject implements ICDIMemoryBlock {

	MIDataReadMemoryInfo mem;
	String expression;
	boolean frozen;
	boolean dirty;

	public MemoryBlock(ICDITarget target, String exp, MIDataReadMemoryInfo info) {
		super(target);
		expression = exp;
		mem = info;
		frozen = true;
	}

	/**
	 * @return the expression use to create the block.
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * Reset the internal MIDataReadMemoryInfo.
	 */
	public void setMIDataReadMemoryInfo(MIDataReadMemoryInfo m) {
		mem = m;
	}

	/**
	 * @return the internal MIDataReadMemoryInfo.
	 */
	public MIDataReadMemoryInfo getMIDataReadMemoryInfo() {
		return mem;
	}

	/**
	 * @return true if any address in the array is within the block.
	 */
	public boolean contains(Long[] adds) {
		for (int i = 0; i < adds.length; i++) {
			if (contains(adds[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return true if the address is within the block.
	 */
	public boolean contains(Long addr) {
		long start = getStartAddress();
		long length = getLength();
		if (start <= addr.longValue() && addr.longValue() <= start + length) {
			return true;
		}
		return false;
	}

	/**
	 * Use by the EventManager to check fire events when doing refresh().
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Use by the EventManager to check fire events when doing refresh().
	 */
	public void setDirty(boolean d) {
		dirty = d;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock#getBytes()
	 */
	public byte[] getBytes() throws CDIException {
		MIMemory[] miMem = mem.getMemories();
		List aList = new ArrayList();
		for (int i = 0; i < miMem.length; i++) {
			long[] data = miMem[i].getData();
			for (int j = 0; j < data.length; j++) {
					aList.add(new Long(data[j]));
			}
		}
		byte[] bytes = new byte[aList.size()];
		for (int i = 0; i < aList.size(); i++) {
			Long l = (Long)aList.get(i);
			bytes[i] = l.byteValue();
		}
		return bytes;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock#refresh()
	 */
	public void refresh() throws CDIException {
		MemoryManager mgr = (MemoryManager)getTarget().getSession().getMemoryManager();
		setDirty(true);
		Long[] addresses = mgr.update(this, null);
		// Check if this affects other blocks.
		if (addresses.length > 0) {
			MemoryBlock[] blocks = mgr.listMemoryBlocks();
			for (int i = 0; i < blocks.length; i++) {
				if (! blocks[i].equals(this) && blocks[i].contains(addresses)) {
					blocks[i].setDirty(true);
					mgr.update(blocks[i], null);
				}
			}
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock#getLength()
	 */
	public long getLength() {
		return mem.getTotalBytes();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock#getStartAddress()
	 */
	public long getStartAddress() {
		return mem.getAddress();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock#isFrozen()
	 */
	public boolean isFrozen() {
		return frozen;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock#setFrozen(boolean)
	 */
	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock#setValue(long, byte[])
	 */
	public void setValue(long offset, byte[] bytes) throws CDIException {
		if (offset >= getLength() || offset + bytes.length > getLength()) {
			throw new CDIException(CdiResources.getString("cdi.model.MemoryBlock.Bad_Offset")); //$NON-NLS-1$
		}
		Session session = (Session)getTarget().getSession();
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		for (int i = 0; i < bytes.length; i++) {
			long l = new Byte(bytes[i]).longValue() & 0xff;
			String value = "0x" + Long.toHexString(l); //$NON-NLS-1$
			MIDataWriteMemory mw = factory.createMIDataWriteMemory(offset + i,
				expression, MIFormat.HEXADECIMAL, 1, value);
			try {
				mi.postCommand(mw);
				MIInfo info = mw.getMIInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
				}
			} catch (MIException e) {
				throw new MI2CDIException(e);
			}
		}
		// If the assign was succesfull fire a MIChangedEvent() via refresh.
		refresh();
	}

}
