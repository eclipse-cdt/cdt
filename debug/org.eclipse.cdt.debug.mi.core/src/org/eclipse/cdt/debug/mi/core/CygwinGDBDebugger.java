/*
 * (c) Copyright Rational Software Corporation. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.CygwinCommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSet;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
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
	protected void initializeLibraries(ILaunchConfiguration config, Session session) throws CDIException {
	}

	public ICDISession createLaunchSession(
		ILaunchConfiguration config,
		IFile exe)
		throws CDIException {
		Session session = (Session) super.createLaunchSession(config, exe);
		session.getMISession().setCommandFactory(commandFactory);
		// For windows we need to start the inferior in a new console window
		// to separate the Inferior std{in,out,err} from gdb std{in,out,err}
		MISession mi = session.getMISession();
		try {
			CommandFactory factory = mi.getCommandFactory();
			MIGDBSet set = factory.createMIGDBSet(new String[]{"new-console"});
			mi.postCommand(set);
			MIInfo info = set.getMIInfo();
			if (info == null) {
				throw new MIException("No answer");
			}
		} catch (MIException e) {
			// We ignore this exception, for example
			// on GNU/Linux the new-console is an error.
		}
		return session;
	}

	public ICDISession createAttachSession(
		ILaunchConfiguration config,
		IFile exe,
		int pid)
		throws CDIException {
		Session session =
			(Session) super.createAttachSession(config, exe, pid);
		session.getMISession().setCommandFactory(commandFactory);
		return session;
	}

	public ICDISession createCoreSession(
		ILaunchConfiguration config,
		IFile exe,
		IPath corefile)
		throws CDIException {
		Session session =
			(Session) super.createCoreSession(config, exe, corefile);
		session.getMISession().setCommandFactory(commandFactory);
		return session;
	}
}
