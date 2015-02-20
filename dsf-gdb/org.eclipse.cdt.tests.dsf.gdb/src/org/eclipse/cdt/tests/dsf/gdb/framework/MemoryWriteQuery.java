package org.eclipse.cdt.tests.dsf.gdb.framework;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;

public class MemoryWriteQuery extends Query<Void> {

	private IMemory fMemoryService;
	private IMemoryDMContext fMemoryDmc;
	private IAddress fBaseAddress;
	private int fOffset;
	private int fWordSize;
	private int fCount;
	private byte[] fBuffer;

	public MemoryWriteQuery(IMemory fMemoryService, IMemoryDMContext memoryDmc,
			IAddress baseAddress, int offset, int wordSize, int count,
			byte[] buffer) {
		this.fMemoryService = fMemoryService;
		this.fMemoryDmc = memoryDmc;
		this.fBaseAddress = baseAddress;
		this.fOffset = offset;
		this.fWordSize = wordSize;
		this.fCount = count;
		this.fBuffer = buffer;
	}

	@Override
	protected void execute(DataRequestMonitor<Void> rm) {
		fMemoryService.setMemory(fMemoryDmc, fBaseAddress, fOffset, fWordSize,
				fCount, fBuffer, rm);
	}

}
