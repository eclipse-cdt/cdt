package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MemoryBlock extends CObject implements ICDIMemoryBlock {

	public MemoryBlock(CTarget target) {
		super(target);
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock#getBytes()
	 */
	public byte[] getBytes() throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock#getLength()
	 */
	public long getLength() {
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock#getStartAddress()
	 */
	public long getStartAddress() {
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock#isFrozen()
	 */
	public boolean isFrozen() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock#setFrozen(boolean)
	 */
	public void setFrozen(boolean frozen) {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock#setValue(long, byte[])
	 */
	public void setValue(long offset, byte[] bytes) throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock#supportsValueModification()
	 */
	public boolean supportsValueModification() {
		return false;
	}

}
