/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICMemoryManager;
import org.eclipse.cdt.debug.core.cdi.ICSession;
import org.eclipse.cdt.debug.core.cdi.model.ICMemoryBlock;
import org.eclipse.cdt.debug.mi.core.MISession;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MemoryManager implements ICMemoryManager {

	MISession session;
	
	public MemoryManager(MISession s) {
		session = s;
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICMemoryManager#addBlock(ICMemoryBlock)
	 */
	public void addBlock(ICMemoryBlock memoryBlock) throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICMemoryManager#getBlock(String)
	 */
	public ICMemoryBlock getBlock(String id) throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICMemoryManager#getBlocks()
	 */
	public ICMemoryBlock[] getBlocks() throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICMemoryManager#removeAllBlocks()
	 */
	public void removeAllBlocks() throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICMemoryManager#removeBlock(ICMemoryBlock)
	 */
	public void removeBlock(ICMemoryBlock memoryBlock) {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICMemoryManager#removeBlocks(ICMemoryBlock[])
	 */
	public void removeBlocks(ICMemoryBlock[] memoryBlocks)
		throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICSessionObject#getSession()
	 */
	public ICSession getSession() {
		return null;
	}

}
