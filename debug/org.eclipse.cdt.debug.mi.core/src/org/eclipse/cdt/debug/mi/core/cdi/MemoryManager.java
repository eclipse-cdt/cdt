/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MemoryManager extends SessionObject implements ICDIMemoryManager {

	public MemoryManager(CSession session) {
		super(session);
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager#addBlock(ICDIMemoryBlock)
	 */
	public void addBlock(ICDIMemoryBlock memoryBlock) throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager#getBlock(String)
	 */
	public ICDIMemoryBlock getBlock(String id) throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager#getBlocks()
	 */
	public ICDIMemoryBlock[] getBlocks() throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager#removeAllBlocks()
	 */
	public void removeAllBlocks() throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager#removeBlock(ICDIMemoryBlock)
	 */
	public void removeBlock(ICDIMemoryBlock memoryBlock) {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIMemoryManager#removeBlocks(ICDIMemoryBlock[])
	 */
	public void removeBlocks(ICDIMemoryBlock[] memoryBlocks)
		throws CDIException {
	}

}
