/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.io.File;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICSession;
import org.eclipse.cdt.debug.core.cdi.ICSourceManager;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SourceManager extends SessionObject implements ICSourceManager {

	public SourceManager(CSession session) {
		super(session);
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICSourceManager#getDirectories()
	 */
	public File[] getDirectories() throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICSourceManager#reset()
	 */
	public void reset() throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICSourceManager#set(File[])
	 */
	public void set(File[] directories) throws CDIException {
	}

}
