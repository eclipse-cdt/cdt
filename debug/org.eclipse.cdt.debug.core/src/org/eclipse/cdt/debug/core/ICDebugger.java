/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.core;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;

public interface ICDebugger {
	public ICDISession createLaunchSession(ILaunchConfiguration config, IFile exe) throws CDIException ;
	public ICDISession createAttachSession(ILaunchConfiguration config, IFile exe, int pid) throws CDIException;
	public ICDISession createCoreSession(ILaunchConfiguration config, IFile exe, IPath corefile) throws CDIException;
}
