package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.debug.service.IMemory;

/**
 * @since 4.2
 */
public interface IGDBMemory extends IMemory {

	/**
	 * Returns the address size (in bytes) of the memory specified by the given context.
	 */
	public int getAddressSize(IMemoryDMContext context);

	/**
	 * Returns whether the memory specified by the given context is big endian.
	 */
	public boolean isBigEndian(IMemoryDMContext context);
}
