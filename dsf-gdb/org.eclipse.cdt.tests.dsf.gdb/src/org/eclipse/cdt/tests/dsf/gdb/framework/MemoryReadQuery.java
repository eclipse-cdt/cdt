package org.eclipse.cdt.tests.dsf.gdb.framework;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.debug.core.model.MemoryByte;

public class MemoryReadQuery extends Query<MemoryByte[]> {
	private IMemory fMemoryService;
	private IMemoryDMContext fMemoryDmc;
	private IAddress fBaseAddress;
	private int fOffset;
	private int fWordSize;
	private int fCount;

	public MemoryReadQuery(IMemory fMemoryService, IMemoryDMContext memoryDmc,
			IAddress baseAddress, int offset, int wordSize, int count) {
		this.fMemoryService = fMemoryService;
		this.fMemoryDmc = memoryDmc;
		this.fBaseAddress = baseAddress;
		this.fOffset = offset;
		this.fWordSize = wordSize;
		this.fCount = count;
	}

	@Override
	protected void execute(DataRequestMonitor<MemoryByte[]> rm) {
		fMemoryService.getMemory(fMemoryDmc, fBaseAddress, fOffset, fWordSize,
				fCount, rm);
	}
}
