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
import org.eclipse.cdt.debug.mi.core.command.MIGDBSetAutoSolib;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSetSolibSearchPath;
import org.eclipse.cdt.debug.mi.core.command.MIGDBShowDirectories;
import org.eclipse.cdt.debug.mi.core.command.MIGDBShowSolibSearchPath;
import org.eclipse.cdt.debug.mi.core.output.MIGDBShowDirectoriesInfo;
import org.eclipse.cdt.debug.mi.core.output.MIGDBShowSolibSearchPathInfo;
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

	public void setLibraryPaths(String[] libPaths) throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIGDBSetSolibSearchPath solib = factory.createMIGDBSetSolibSearchPath(libPaths);
		try {
			mi.postCommand(solib);
			MIInfo info = solib.getMIInfo();
		} catch (MIException e) {
			throw new CDIException(e.getMessage());
		}
	}

	public String[] getLibraryPaths() throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIGDBShowSolibSearchPath dir = factory.createMIGDBShowSolibSearchPath();
		try {
			mi.postCommand(dir);
			MIGDBShowSolibSearchPathInfo info = dir.getMIGDBShowSolibSearchPathInfo();
			return info.getDirectories();
		} catch (MIException e) {
			throw new CDIException(e.getMessage());
		}
	}

	public void setAutoSolib() throws CDIException {
		MISession mi = getCSession().getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIGDBSetAutoSolib solib = factory.createMIGDBSetAutoSolib();
		try {
			mi.postCommand(solib);
			MIInfo info = solib.getMIInfo();
		} catch (MIException e) {
			throw new CDIException(e.getMessage());
		}
	}

}
