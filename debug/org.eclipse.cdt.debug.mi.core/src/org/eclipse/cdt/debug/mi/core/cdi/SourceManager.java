/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.io.File;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISourceManager;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIEnvironmentDirectory;
import org.eclipse.cdt.debug.mi.core.command.MIGDBShowDirectories;
import org.eclipse.cdt.debug.mi.core.output.MIGDBShowDirectoriesInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;


/**
 */
public class SourceManager extends SessionObject implements ICDISourceManager {

	public SourceManager(CSession session) {
		super(session);
	}
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getDirectories()
	 */
	public File[] getDirectories() throws CDIException {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#reset()
	 */
	public void reset() throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#set(File[])
	 */
	public void set(File[] directories) throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#addSourcePaths(String[])
	 */
	public void addSourcePaths(String[] dirs) throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIEnvironmentDirectory dir = factory.createMIEnvironmentDirectory(dirs);
		try {
			mi.postCommand(dir);
			MIInfo info = dir.getMIInfo();
		} catch (MIException e) {
			throw new CDIException(e.getMessage());
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getSourcePaths()
	 */
	public String[] getSourcePaths() throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIGDBShowDirectories dir = factory.createMIGDBShowDirectories();
		try {
			mi.postCommand(dir);
			MIGDBShowDirectoriesInfo info = dir.getMIGDBShowDirectoriesInfo();
			return info.getDirectories();
		} catch (MIException e) {
			throw new CDIException(e.getMessage());
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#addLibraryPaths(String[])
	 */
	public void addLibraryPaths(String[] libPaths) throws CDIException {
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISourceManager#getLibraryPaths()
	 */
	public String[] getLibraryPaths() throws CDIException {
		return null;
	}

}
