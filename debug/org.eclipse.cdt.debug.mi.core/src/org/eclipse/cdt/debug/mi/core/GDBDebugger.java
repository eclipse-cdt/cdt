/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.ICDebugger;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.SharedLibraryManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;

public class GDBDebugger implements ICDebugger {

	protected void initializeLibraries(ILaunchConfiguration config, Session session) throws CDIException {
		try {
			ICDISharedLibraryManager mgr = session.getSharedLibraryManager();
			if ( mgr instanceof SharedLibraryManager )
			{
				boolean autolib = config.getAttribute(IMILaunchConfigurationConstants.ATTR_AUTO_SOLIB, false);
				((SharedLibraryManager)mgr).setAutoLoadSymbols(autolib);
			}
			List p = config.getAttribute(IMILaunchConfigurationConstants.ATTR_SOLIB_PATH, new ArrayList(1));
			if (p.size() > 0) {
				String[] paths = (String[])p.toArray(new String[0]);
				mgr.setSharedLibraryPaths(paths);
			}
		} catch (CoreException e) {
			throw new CDIException("Error initializing: " + e.getMessage());
		}
	}

	public ICDISession createLaunchSession(ILaunchConfiguration config, IFile exe) throws CDIException {
		try {
			String gdb = config.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, "gdb");
			File cwd = exe.getProject().getLocation().toFile();
			String gdbinit = config.getAttribute(IMILaunchConfigurationConstants.ATTR_GDB_INIT, ".gdbinit");
			Session session = (Session)MIPlugin.getDefault().createCSession(gdb, exe.getLocation().toFile(), cwd, gdbinit);
			initializeLibraries(config, session);
			return session;
		} catch (IOException e) {
			throw new CDIException("Error initializing: " + e.getMessage());
		} catch (MIException e) {
			throw new CDIException("Error initializing: " + e.getMessage());
		} catch (CoreException e) {
			throw new CDIException("Error initializing: " + e.getMessage());
		}
	}

	public ICDISession createAttachSession(ILaunchConfiguration config, IFile exe, int pid) throws CDIException {
		try {
			String gdb = config.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, "gdb");
			File cwd = exe.getProject().getLocation().toFile();
			String gdbinit = config.getAttribute(IMILaunchConfigurationConstants.ATTR_GDB_INIT, ".gdbinit");
			Session session = (Session)MIPlugin.getDefault().createCSession(gdb, exe.getLocation().toFile(), pid, null, cwd, gdbinit);
			initializeLibraries(config, session);
			return session;
		} catch (IOException e) {
			throw new CDIException("Error initializing: " + e.getMessage());
		} catch (MIException e) {
			throw new CDIException("Error initializing: " + e.getMessage());
		} catch (CoreException e) {
			throw new CDIException("Error initializing: " + e.getMessage());
		}

	}

	public ICDISession createCoreSession(ILaunchConfiguration config, IFile exe, IPath corefile) throws CDIException {
		try {
			String gdb = config.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, "gdb");
			File cwd = exe.getProject().getLocation().toFile();
			String gdbinit = config.getAttribute(IMILaunchConfigurationConstants.ATTR_GDB_INIT, ".gdbinit");
			Session session = (Session)MIPlugin.getDefault().createCSession(gdb, exe.getLocation().toFile(), corefile.toFile(), cwd, gdbinit);
			initializeLibraries(config, session);
			return session;
		} catch (IOException e) {
			throw new CDIException("Error initializing: " + e.getMessage());
		} catch (MIException e) {
			throw new CDIException("Error initializing: " + e.getMessage());
		} catch (CoreException e) {
			throw new CDIException("Error initializing: " + e.getMessage());
		}
	}

}
