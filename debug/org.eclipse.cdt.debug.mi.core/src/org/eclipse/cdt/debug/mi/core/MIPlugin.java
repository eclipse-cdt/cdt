/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.command.CLICommand;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MITargetAttach;
import org.eclipse.cdt.debug.mi.core.command.MITargetSelect;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;

/**
 * GDB/MI Plugin.
 */
public class MIPlugin extends Plugin {

	/**
	 * The plug-in identifier of the Java core support
	 * (value <code>"org.eclipse.jdt.core"</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.cdt.debug.mi.core" ; //$NON-NLS-1$

	//The shared instance.
	private static MIPlugin plugin;

	// GDB init command file
	private static final String GDBINIT = ".gdbinit";

	// GDB command
	private static final String GDB = "gdb";

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
	 * @param PTY
	 * @param int
	 * @param int
	 * @throws MIException
	 * @return MISession
	 */
	public MISession createMISession(Process process, PTY pty, int timeout, int type) throws MIException {
		return new MISession(process, pty, timeout, type);
	}

	/**
	 * Method createMISession.
	 * @param Process
	 * @param PTY
	 * @param type
	 * @throws MIException
	 * @return MISession
	 */
	public MISession createMISession(Process process, PTY pty, int type) throws MIException {
		MIPlugin plugin = getDefault();
		Preferences prefs = plugin.getPluginPreferences();
		int timeout = prefs.getInt(IMIConstants.PREF_REQUEST_TIMEOUT);
		return createMISession(process, pty, timeout, type);
	}

	/**
	 * Method createCSession.
	 * @param program
	 * @return ICDISession
	 * @throws MIException
	 */
	public ICDISession createCSession(String gdb, File program, File cwd, String gdbinit) throws IOException, MIException {
		PTY pty = null;
		try {
			pty = new PTY();
		} catch (IOException e) {
		}
		return createCSession(gdb, program, cwd, gdbinit, pty);
	}

	/**
	 * Method createCSession.
	 * @param program
	 * @return ICDISession
	 * @throws IOException
	 */
	public ICDISession createCSession(String gdb, File program, File cwd, String gdbinit, PTY pty) throws IOException, MIException {
		if (gdb == null || gdb.length() == 0) {
			gdb =  GDB;
		}
		
		if (gdbinit == null || gdbinit.length() == 0) {
			gdbinit = GDBINIT;
		}

		String[] args;
		if (pty != null) {
			if (program == null) {
				args = new String[] {gdb, "--cd="+cwd.getAbsolutePath(), "--command="+gdbinit, "-q", "-nw", "-tty", pty.getSlaveName(), "-i", "mi1"};
			} else {
				args = new String[] {gdb, "--cd="+cwd.getAbsolutePath(), "--command="+gdbinit, "-q", "-nw", "-tty", pty.getSlaveName(), "-i", "mi1", program.getAbsolutePath()};
			}
		} else {
			if (program == null) {
				args = new String[] {gdb, "--cd="+cwd.getAbsolutePath(), "--command="+gdbinit, "-q", "-nw", "-i", "mi1"};
			} else {
				args = new String[] {gdb, "--cd="+cwd.getAbsolutePath(), "--command="+gdbinit, "-q", "-nw", "-i", "mi1", program.getAbsolutePath()};
			}
		}

		Process pgdb = ProcessFactory.getFactory().exec(args);
		MISession session;
		try {
			session = createMISession(pgdb, pty, MISession.PROGRAM);
		} catch (MIException e) {
			pgdb.destroy();
			throw e;
		}
		// Try to detect if we have been attach via "target remote localhost:port"
		// and set the state to be suspended.
		try {
			CLICommand cmd = new CLICommand("info remote-process");
			session.postCommand(cmd);
			MIInfo info = cmd.getMIInfo();
			if (info == null) {
				pgdb.destroy();
				throw new MIException("No answer");
			}
			//@@@ We have to manually set the suspended state when we attach
			session.getMIInferior().setSuspended();
			session.getMIInferior().update();
		} catch (MIException e) {
			// If an exception is thrown that means ok
			// we did not attach to any target.
		}
		return new Session(session, false);
	}

	/**
	 * Method createCSession.
	 * @param program
	 * @param core
	 * @return ICDISession
	 * @throws IOException
	 */
	public ICDISession createCSession(String gdb, File program, File core, File cwd, String gdbinit) throws IOException, MIException {
		if (gdb == null || gdb.length() == 0) {
			gdb =  GDB;
		}
		
		if (gdbinit == null || gdbinit.length() == 0) {
			gdbinit = GDBINIT;
		}
		
		String[] args;
		if (program == null) {
			args = new String[] {gdb, "--cd="+cwd.getAbsolutePath(), "--command="+gdbinit, "--quiet", "-nw", "-i", "mi1", "-c", core.getAbsolutePath()};
		} else {
			args = new String[] {gdb, "--cd="+cwd.getAbsolutePath(), "--command="+gdbinit, "--quiet", "-nw", "-i", "mi1", "-c", core.getAbsolutePath(), program.getAbsolutePath()};
		}
		Process pgdb = ProcessFactory.getFactory().exec(args);
		MISession session;
		try {
			session = createMISession(pgdb, null, MISession.CORE);
		} catch (MIException e) {
			pgdb.destroy();
			throw e;
		}
		return new Session(session);
	}

	/**
	 * Method createCSession.
	 * @param program
	 * @param pid
	 * @return ICDISession
	 * @throws IOException
	 */
	public ICDISession createCSession(String gdb, File program, int pid, String[] targetParams, File cwd, String gdbinit) throws IOException, MIException {
		if (gdb == null || gdb.length() == 0) {
			gdb =  GDB;
		}

		if (gdbinit == null || gdbinit.length() == 0) {
			gdbinit = GDBINIT;
		}

		String[] args;
		if (program == null) {
			args = new String[] {gdb, "--cd="+cwd.getAbsolutePath(), "--command="+gdbinit, "--quiet", "-nw", "-i", "mi1"};
		} else {
			args = new String[] {gdb, "--cd="+cwd.getAbsolutePath(), "--command="+gdbinit, "--quiet", "-nw", "-i", "mi1", program.getAbsolutePath()};
		}
		Process pgdb = ProcessFactory.getFactory().exec(args);
		MISession session;
		try {
			session = createMISession(pgdb, null, MISession.ATTACH);
		} catch (MIException e) {
			pgdb.destroy();
			throw e;
		}
		CommandFactory factory = session.getCommandFactory();
		if (targetParams != null && targetParams.length > 0) {
			MITargetSelect target = factory.createMITargetSelect(targetParams);
			session.postCommand(target);
			MIInfo info = target.getMIInfo();
			if (info == null) {
				pgdb.destroy();
				throw new MIException("No answer");
			}
		}
		if (pid > 0) {
			MITargetAttach attach = factory.createMITargetAttach(pid);
			session.postCommand(attach);
			MIInfo info = attach.getMIInfo();
			if (info == null) {
				pgdb.destroy();
				throw new MIException("No answer");
			}
		}
		//@@@ We have to manually set the suspended state when we attach
		session.getMIInferior().setSuspended();
		session.getMIInferior().update();
		return new Session(session, true);
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID;
		}
		return getDefault().getDescriptor().getUniqueIdentifier();
	}

	public void debugLog(String message) {
		if (getDefault().isDebugging()) {			
			// Time stamp
			message = MessageFormat.format( "[{0}] {1}", new Object[] { new Long( System.currentTimeMillis() ), message } );
			// This is to verbose for a log file, better use the console.
			//	getDefault().getLog().log(StatusUtil.newStatus(Status.ERROR, message, null));
			// ALERT:FIXME: For example for big buffers say 4k length,
			// the console will simply blow taking down eclipse.
			// This seems only to happen in Eclipse-gtk and Eclipse-motif
			// on GNU/Linux, so it will be break in smaller chunks.
			while (message.length() > 100) {
				String partial = message.substring(0, 100);
				message = message.substring(100);
				System.err.println(partial + "\\");
			}
			if (message.endsWith("\n")) {
				System.err.print(message);
			} else {
				System.err.println(message);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#startup()
	 */
	public void startup() throws CoreException {
		super.startup();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#initializeDefaultPluginPrefrences()
	 */
	protected void initializeDefaultPluginPreferences() {
		getPluginPreferences().setDefault(IMIConstants.PREF_REQUEST_TIMEOUT, MISession.REQUEST_TIMEOUT);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#shutdown()
	 */
	public void shutdown() throws CoreException {
		savePluginPreferences();
		super.shutdown();
	}

}
