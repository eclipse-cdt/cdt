/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core;

import java.io.IOException;

import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.mi.core.cdi.CSession;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSet;
import org.eclipse.cdt.debug.mi.core.command.MITargetAttach;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Plugin;

/**
 * GDB/MI Plugin.
 */
public class MIPlugin extends Plugin {
	//The shared instance.
	private static MIPlugin plugin;

	/**
	 * The constructor
	 * @see org.eclipse.core.runtime.Plugin#Plugin(IPluginDescriptor)
	 */
	public MIPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}

	/**
	 * Returns the singleton.
	 */
	public static MIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Method createMISession.
	 * @param Process
	 * @return MISession
	 */
	public MISession createMISession(Process process) throws MIException {
		return new MISession(process);
	}

	/**
	 * Method createMISession.
	 * @param Process
	 * @param PTY
	 * @return MISession
	 */
	public MISession createMISession(Process process, PTY pty) throws MIException {
		return new MISession(process, pty);
	}

	/**
	 * Method createCSession.
	 * @param program
	 * @return ICDISession
	 * @throws IOException
	 */
	public ICDISession createCSession(String gdb, String program) throws IOException, MIException {
		if (gdb == null || gdb.length() == 0) {
			gdb =  "gdb";
		}

		String[] args;
		PTY pty = null;
		try {
			pty = new PTY();
			String ttyName = pty.getSlaveName();
			args = new String[] {gdb, "-q", "-nw", "-tty", ttyName, "-i", "mi1", program};
		} catch (IOException e) {
			//e.printStackTrace();
			pty = null;
			args = new String[] {"gdb", "-q", "-nw", "-i", "mi1", program};
		}

		Process pgdb = ProcessFactory.getFactory().exec(args);
		MISession session = createMISession(pgdb, pty);
		// For windows we need to start the inferior in a new console window
		// to separate the Inferior std{in,out,err} from gdb std{in,out,err}
		try {
			CommandFactory factory = session.getCommandFactory();
			MIGDBSet set = factory.createMIGDBSet(new String[]{"new-console"});
			session.postCommand(set);
			MIInfo info = set.getMIInfo();
			if (info == null) {
				throw new IOException("No answer");
			}
		} catch (MIException e) {
			//throw new IOException("Failed to attach");
		}
		return new CSession(session, false);
	}

	/**
	 * Method createCSession.
	 * @param program
	 * @param core
	 * @return ICDISession
	 * @throws IOException
	 */
	public ICDISession createCSession(String gdb, String program, String core) throws IOException, MIException {
		if (gdb == null || gdb.length() == 0) {
			gdb =  "gdb";
		}
		String[] args = new String[] {gdb, "--quiet", "-nw", "-i", "mi1", program, core};
		Process pgdb = ProcessFactory.getFactory().exec(args);
		MISession session = createMISession(pgdb);
		return new CSession(session);
	}

	/**
	 * Method createCSession.
	 * @param program
	 * @param pid
	 * @return ICDISession
	 * @throws IOException
	 */
	public ICDISession createCSession(String gdb, String program, int pid) throws IOException, MIException {
		if (gdb == null || gdb.length() == 0) {
			gdb =  "gdb";
		}
		String[] args = new String[] {gdb, "--quiet", "-nw", "-i", "mi1", program};
		Process pgdb = ProcessFactory.getFactory().exec(args);
		MISession session = createMISession(pgdb);
		try {
			CommandFactory factory = session.getCommandFactory();
			MITargetAttach attach = factory.createMITargetAttach(pid);
			session.postCommand(attach);
			MIInfo info = attach.getMIInfo();
			if (info == null) {
				throw new IOException("No answer");
			}
			//@@@ We have to manually set the suspended state when we attach
			session.getMIInferior().setSuspended();
		} catch (MIException e) {
			throw new IOException("Failed to attach");
		}
		return new CSession(session, true);
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return "org.eclipse.cdt.debug.mi.core"; //$NON-NLS-1$
		}
		return getDefault().getDescriptor().getUniqueIdentifier();
	}

	public static void debugLog(String message) {
		//	if ( getDefault().isDebugging() ) {
		//		getDefault().getLog().log(StatusUtil.newStatus(Status.ERROR, message, null));
		if (message.endsWith("\n")) {
			System.err.print(message);
		} else {
			System.err.println(message);
		}
		//	}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#startup()
	 */
	public void startup() throws CoreException {
		super.startup();
		getPluginPreferences().setDefault( IMIConstants.PREF_REQUEST_TIMEOUT, IMIConstants.DEF_REQUEST_TIMEOUT );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#shutdown()
	 */
	public void shutdown() throws CoreException {
		savePluginPreferences();
		super.shutdown();
	}

}
