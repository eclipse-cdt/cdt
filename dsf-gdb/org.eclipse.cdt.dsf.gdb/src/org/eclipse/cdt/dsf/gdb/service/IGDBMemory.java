package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IMemory;

/**
 * @since 4.2
 */
public interface IGDBMemory extends IMemory {

	public void getAddressSize(IMemoryDMContext context, DataRequestMonitor<Integer> rm);
}
