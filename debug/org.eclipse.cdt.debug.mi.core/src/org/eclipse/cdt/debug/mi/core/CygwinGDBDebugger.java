/*
 * (c) Copyright Rational Software Corporation. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.mi.core.cdi.CSession;
import org.eclipse.cdt.debug.mi.core.command.CygwinCommandFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Cygwin GDB Debugger overrides the GDB Debugger to apply the Cygwin
 * Command Factory to the MI Session.
 */
public class CygwinGDBDebugger extends GDBDebugger {

	static final CygwinCommandFactory commandFactory =
		new CygwinCommandFactory();

	/* Cygwin does not have any special initialization like solib paths etc.. */
	protected void initializeLibraries(ILaunchConfiguration config, CSession session) throws CDIException {
	}

	public ICDISession createLaunchSession(
		ILaunchConfiguration config,
		IFile exe)
		throws CDIException {
		CSession session = (CSession) super.createLaunchSession(config, exe);
		session.getMISession().setCommandFactory(commandFactory);
		return session;
	}

	public ICDISession createAttachSession(
		ILaunchConfiguration config,
		IFile exe,
		int pid)
		throws CDIException {
		CSession session =
			(CSession) super.createAttachSession(config, exe, pid);
		session.getMISession().setCommandFactory(commandFactory);
		return session;
	}

	public ICDISession createCoreSession(
		ILaunchConfiguration config,
		IFile exe,
		IPath corefile)
		throws CDIException {
		CSession session =
			(CSession) super.createCoreSession(config, exe, corefile);
		session.getMISession().setCommandFactory(commandFactory);
		return session;
	}
}
