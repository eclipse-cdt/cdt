/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.mi.core.cdi.SharedLibraryManager;
import org.eclipse.cdt.debug.mi.core.output.MIShared;

/**
 * Place holder for shared library info.
 */
public class SharedLibrary extends CObject implements ICDISharedLibrary {

	SharedLibraryManager mgr;
	MIShared miShared;

	public SharedLibrary(SharedLibraryManager m, MIShared slib) {
		super(m.getSession().getCurrentTarget());
		mgr = m;
		miShared = slib;
	}

	public void setMIShared(MIShared slib) {
		miShared = slib;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary#getFileName()
	 */
	public String getFileName() {
		return miShared.getName();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary#getStartAddress()
	 */
	public long getStartAddress() {
		return miShared.getFrom();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary#getEndAddress()
	 */
	public long getEndAddress() {
		return miShared.getTo();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary#areSymbolsLoaded()
	 */
	public boolean areSymbolsLoaded() {
		return miShared.isRead();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary#loadSymbols()
	 */
	public void loadSymbols() throws CDIException {
		mgr.loadSymbols(new ICDISharedLibrary[] { this });
	}

}
