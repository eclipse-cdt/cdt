package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.mi.core.output.MIDataReadMemoryInfo;
import org.eclipse.cdt.debug.mi.core.output.MIMemory;

/**
 */
public class MemoryBlock extends CObject implements ICDIMemoryBlock {

	MIDataReadMemoryInfo mem;
	boolean frozen;

	public MemoryBlock(CTarget target, MIDataReadMemoryInfo info) {
		super(target);
		mem = info;
		frozen = true;
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
		throw new CDIException("Not supported");
	}

}
