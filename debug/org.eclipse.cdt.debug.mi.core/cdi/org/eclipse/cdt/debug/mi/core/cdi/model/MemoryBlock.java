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

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
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

	String expression;
	boolean frozen;
	boolean dirty;

	private MIDataReadMemoryInfo mem;
	private BigInteger cStartAddress; //cached start address
	private byte[] cBytes; //cached bytes
	private int[] badOffsets;

	public MemoryBlock(Target target, String exp, MIDataReadMemoryInfo info) {
		super(target);
		expression = exp;
		frozen = true;
		setMIDataReadMemoryInfo(info);
	}

	/**
	 * @return the expression use to create the block.
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * Reset the internal MIDataReadMemoryInfo. All modifications into mem info should be done
	 * using this method
	 */
	public void setMIDataReadMemoryInfo(MIDataReadMemoryInfo m) {
		cStartAddress = MIFormat.getBigInteger(m.getAddress());
		cBytes = getBytes(m);
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
	public boolean contains(BigInteger[] adds) {
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
	public boolean contains(BigInteger addr) {
		BigInteger start = getStartAddress();
		long length = getLength();
		if ( start.compareTo(addr) <= 0 && 
		     addr.compareTo(start.add(BigInteger.valueOf(length))) <= 0 ) 
		{
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
	 * 
	 */
	private byte[] getBytes(MIDataReadMemoryInfo m) {
		byte[] bytes = new byte[0];

		// sanity.
		if (m == null) {
			return bytes;
		}

		// collect the data
		MIMemory[] miMem = m.getMemories();
		for (int i = 0; i < miMem.length; ++i) {
			long[] data = miMem[i].getData();
			if (data.length > 0) {
				int blen = bytes.length;
				byte[] newBytes = new byte[blen + data.length];
				System.arraycopy(bytes, 0, newBytes, 0, blen);
				for (int j = 0; j < data.length; ++j, ++blen) {
					newBytes[blen] = (byte)data[j];
				}
				bytes = newBytes;
			}
		}
		return bytes;
	}

	private int[] getBadOffsets(MIDataReadMemoryInfo m) {
		int[] offsets = new int[0];

		// sanity
		if (m == null) {
			return offsets;
		}

		// collect the data
		MIMemory[] miMem = m.getMemories();
		for (int i = 0; i < miMem.length; i++) {
			int[] data = miMem[i].getBadOffsets();
			if (data.length > 0) {
				int olen = offsets.length;
				int[] newOffsets = new int[olen + data.length];
				System.arraycopy(offsets, 0, newOffsets, 0, olen);
				System.arraycopy(data, 0, newOffsets, olen, data.length);
				offsets = newOffsets;
			}
		}
		return offsets;
	}

	public byte[] getBytes() throws CDIException {
		return cBytes;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock#refresh()
	 */
	public void refresh() throws CDIException {
		Target target = (Target)getTarget();
		MemoryManager mgr = ((Session)target.getSession()).getMemoryManager();
		setDirty(true);
		BigInteger[] addresses = mgr.update(this, null);
		// Check if this affects other blocks.
		if (addresses.length > 0) {
			MemoryBlock[] blocks = mgr.getMemoryBlocks(target.getMISession());
			for (int i = 0; i < blocks.length; i++) {
				MemoryBlock block = blocks[i];
				if (! block.equals(this) && block.contains(addresses)) {
					block.setDirty(true);
					mgr.update(block, null);
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
	public BigInteger getStartAddress() {
		return cStartAddress;
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
		MISession miSession = ((Target)getTarget()).getMISession();
		CommandFactory factory = miSession.getCommandFactory();
		for (int i = 0; i < bytes.length; i++) {
			long l = new Byte(bytes[i]).longValue() & 0xff;
			String value = "0x" + Long.toHexString(l); //$NON-NLS-1$
			MIDataWriteMemory mw = factory.createMIDataWriteMemory(offset + i,
				expression, MIFormat.HEXADECIMAL, 1, value);
			try {
				miSession.postCommand(mw);
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock#getFlags(int)
	 */
	public synchronized byte getFlags(int offset) {
		if (badOffsets == null) {
			badOffsets = getBadOffsets(mem);
		}
		if (badOffsets != null) {
			for (int i = 0; i < badOffsets.length; ++i) {
				if (badOffsets[i] == offset) {
					return 0;
				}
			}
			return VALID;
		}
		return 0;
	}

}
