/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core;

import java.io.IOException;

import org.eclipse.cdt.debug.core.ICDebugger;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;

public class GDBDebugger implements ICDebugger {

	public ICDISession createLaunchSession(ILaunchConfiguration config, IFile exe) throws CDIException {
		try {
			return MIPlugin.getDefault().createCSession(exe.getLocation().toOSString());
		}
		catch (IOException e) {
			throw new CDIException("Error initializing: " + e.getMessage());
		}
		catch (MIException e) {
			throw new CDIException("Error initializing: " + e.getMessage());
		}
	}

	public ICDISession createAttachSession(ILaunchConfiguration config, IFile exe, int pid) throws CDIException {
		try {
			return MIPlugin.getDefault().createCSession(exe.getLocation().toOSString(), pid);
		}
		catch (IOException e) {
			throw new CDIException("Error initializing: " + e.getMessage());
		}
		catch (MIException e) {
			throw new CDIException("Error initializing: " + e.getMessage());
		}

	}

	public ICDISession createCoreSession(ILaunchConfiguration config, IFile exe, IPath corefile) throws CDIException {
		try {
			return MIPlugin.getDefault().createCSession(exe.getLocation().toOSString(), corefile.toOSString());
		}
		catch (IOException e) {
			throw new CDIException("Error initializing: " + e.getMessage());
		}
		catch (MIException e) {
			throw new CDIException("Error initializing: " + e.getMessage());
		}
		
	}

}
