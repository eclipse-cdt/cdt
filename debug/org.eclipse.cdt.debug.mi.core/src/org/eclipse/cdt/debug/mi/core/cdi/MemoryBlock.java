package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MIFormat;
import org.eclipse.cdt.debug.mi.core.MISession;
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

	public MemoryBlock(CTarget target, String exp, MIDataReadMemoryInfo info) {
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
		MISession mi = getCTarget().getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		for (int i = 0; i < bytes.length; i++) {
			long l = new Byte(bytes[i]).longValue();
			String value = "0x" + Long.toHexString(l);
			MIDataWriteMemory mem = factory.createMIDataWriteMemory(offset + i,
				expression, MIFormat.HEXADECIMAL, 1, value);
			try {
				mi.postCommand(mem);
				MIInfo info = mem.getMIInfo();
				if (info == null) {
					throw new CDIException("No answer");
				}
			} catch (MIException e) {
				throw new CDIException(e.getMessage());
			}
		}
	}

}
