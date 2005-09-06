/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.command.CLITargetAttach;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIStackListFrames;
import org.eclipse.cdt.debug.mi.core.command.MITargetSelect;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.osgi.framework.BundleContext;

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
	private static final String GDBINIT = ".gdbinit"; //$NON-NLS-1$

	// GDB command
	private static final String GDB = "gdb"; //$NON-NLS-1$

	private static ResourceBundle fgResourceBundle;
	static {
		try {
			fgResourceBundle = ResourceBundle.getBundle("org.eclipse.cdt.debug.mi.core.MIPluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			fgResourceBundle = null;
		}
	}
	/**
	 * The constructor
	 * @see org.eclipse.core.runtime.Plugin#Plugin()
	 */
	public MIPlugin() {
		super();
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
	public MISession createMISession(MIProcess process, IMITTY pty, int timeout, int type, int launchTimeout, String miVersion, IProgressMonitor monitor) throws MIException {
		return new MISession(process, pty, type, timeout, launchTimeout, miVersion, monitor);
	}

	/**
	 * Method createMISession.
	 * @param Process
	 * @param PTY
	 * @param type
	 * @throws MIException
	 * @return MISession
	 */
	public MISession createMISession(MIProcess process, IMITTY pty, int type, String miVersion, IProgressMonitor monitor) throws MIException {
		MIPlugin miPlugin = getDefault();
		Preferences prefs = miPlugin.getPluginPreferences();
		int timeout = prefs.getInt(IMIConstants.PREF_REQUEST_TIMEOUT);
		int launchTimeout = prefs.getInt(IMIConstants.PREF_REQUEST_LAUNCH_TIMEOUT);
		return createMISession(process, pty, timeout, type, launchTimeout, miVersion, monitor);
	}

	/**
	 * Method createCSession; Create an new PTY instance and launch gdb in mi for local debug.
	 * 
	 * @param program
	 * @return ICDISession
	 * @throws MIException
	 */
	public Session createCSession(String gdb, String miVersion, File program, File cwd, String gdbinit, IProgressMonitor monitor) throws IOException, MIException {
		IMITTY pty = null;
		boolean failed = false;

		try {
			PTY pseudo = new PTY();
			pty = new MITTYAdapter(pseudo);
		} catch (IOException e) {
			// Should we not print/log this ?
		}

		try {
			return createCSession(gdb, miVersion, program, cwd, gdbinit, pty, monitor);
		} catch (IOException exc) {
			failed = true;
			throw exc;
		} catch (MIException exc) {
			failed = true;
			throw exc;
		} finally {
			if (failed) {
				// Shutdown the pty console.
				if (pty != null) {
					try {
						OutputStream out = pty.getOutputStream();
						if (out != null) {
							out.close();
						}
						InputStream in = pty.getInputStream();
						if (in != null) {
							in.close();
						}
					} catch (IOException e) {
					}
				}
			}
		}
	}

	/**
	 * Method createCSession; lauch gdb in mi mode for local debugging
	 * @param program
	 * @return ICDISession
	 * @throws IOException
	 */
	public Session createCSession(String gdb, String miVersion, File program, File cwd, String gdbinit, IMITTY pty, IProgressMonitor monitor) throws IOException, MIException {
		if (gdb == null || gdb.length() == 0) {
			gdb =  GDB;
		}
		
		if (gdbinit == null || gdbinit.length() == 0) {
			gdbinit = GDBINIT;
		}

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		String[] args;
		if (pty != null) {
			if (program == null) {
				args = new String[] {gdb, "--cd="+cwd.getAbsolutePath(), "--command="+gdbinit, "-q", "-nw", "-tty", pty.getSlaveName(), "-i", miVersion}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			} else {
				args = new String[] {gdb, "--cd="+cwd.getAbsolutePath(), "--command="+gdbinit, "-q", "-nw", "-tty", pty.getSlaveName(), "-i", miVersion, program.getAbsolutePath()}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			}
		} else {
			if (program == null) {
				args = new String[] {gdb, "--cd="+cwd.getAbsolutePath(), "--command="+gdbinit, "-q", "-nw", "-i", miVersion}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			} else {
				args = new String[] {gdb, "--cd="+cwd.getAbsolutePath(), "--command="+gdbinit, "-q", "-nw", "-i", miVersion, program.getAbsolutePath()}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			}
		}

		int launchTimeout = MIPlugin.getDefault().getPluginPreferences().getInt(IMIConstants.PREF_REQUEST_LAUNCH_TIMEOUT);		
		MIProcess pgdb = new MIProcessAdapter(args, launchTimeout, monitor);

		if (MIPlugin.getDefault().isDebugging()) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < args.length; ++i) {
				sb.append(args[i]);
				sb.append(' ');
			}
			MIPlugin.getDefault().debugLog(sb.toString());
		}
		
		MISession session;
		try {
			session = createMISession(pgdb, pty, MISession.PROGRAM, miVersion, monitor);
		} catch (MIException e) {
			pgdb.destroy();
			throw e;
		}
		// Try to detect if we have been attach/connected via "target remote localhost:port"
		// or "attach" and set the state to be suspended.
		try {
			CommandFactory factory = session.getCommandFactory();
			MIStackListFrames frames = factory.createMIStackListFrames();
			session.postCommand(frames);
			MIInfo info = frames.getMIInfo();
			if (info == null) {
				pgdb.destroy();
				throw new MIException(getResourceString("src.common.No_answer")); //$NON-NLS-1$
			}
			//@@@ We have to manually set the suspended state since we have some stackframes
			session.getMIInferior().setSuspended();
			session.getMIInferior().update();
		} catch (MIException e) {
			// If an exception is thrown that means ok
			// we did not attach/connect to any target.
		}
		return new Session(session, false);
	}

	/**
	 * Method createCSession; Post mortem debug with a core file.
	 * @param program
	 * @param core
	 * @return ICDISession
	 * @throws IOException
	 */
	public Session createCSession(String gdb, String miVersion, File program, File core, File cwd, String gdbinit, IProgressMonitor monitor) throws IOException, MIException {
		if (gdb == null || gdb.length() == 0) {
			gdb =  GDB;
		}
		
		if (gdbinit == null || gdbinit.length() == 0) {
			gdbinit = GDBINIT;
		}

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		String[] args;
		if (program == null) {
			args = new String[] {gdb, "--cd="+cwd.getAbsolutePath(), "--command="+gdbinit, "--quiet", "-nw", "-i", miVersion, "-c", core.getAbsolutePath()}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
		} else {
			args = new String[] {gdb, "--cd="+cwd.getAbsolutePath(), "--command="+gdbinit, "--quiet", "-nw", "-i", miVersion, "-c", core.getAbsolutePath(), program.getAbsolutePath()}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
		}

		int launchTimeout = MIPlugin.getDefault().getPluginPreferences().getInt(IMIConstants.PREF_REQUEST_LAUNCH_TIMEOUT);		
		MIProcess pgdb = new MIProcessAdapter(args, launchTimeout, monitor);
		
		if (MIPlugin.getDefault().isDebugging()) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < args.length; ++i) {
				sb.append(args[i]);
				sb.append(' ');
			}
			MIPlugin.getDefault().debugLog(sb.toString());
		}
		
		MISession session;
		try {
			session = createMISession(pgdb, null, MISession.CORE, miVersion, monitor);
			//@@@ We have to manually set the suspended state when doing post-mortem
			session.getMIInferior().setSuspended();
		} catch (MIException e) {
			pgdb.destroy();
			throw e;
		}
		return new Session(session);
	}

	/**
	 * Method createCSession; remote debuging by selectin  a target.
	 * @param program
	 * @param pid
	 * @return ICDISession
	 * @throws IOException
	 */
	public Session createCSession(String gdb, String miVersion, File program, int pid, String[] targetParams, File cwd, String gdbinit, IProgressMonitor monitor) throws IOException, MIException {
		if (gdb == null || gdb.length() == 0) {
			gdb =  GDB;
		}

		if (gdbinit == null || gdbinit.length() == 0) {
			gdbinit = GDBINIT;
		}

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		String[] args;
		if (program == null) {
			args = new String[] {gdb, "--cd="+cwd.getAbsolutePath(), "--command="+gdbinit, "--quiet", "-nw", "-i", miVersion}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		} else {
			args = new String[] {gdb, "--cd="+cwd.getAbsolutePath(), "--command="+gdbinit, "--quiet", "-nw", "-i", miVersion, program.getAbsolutePath()}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		}

		int launchTimeout = MIPlugin.getDefault().getPluginPreferences().getInt(IMIConstants.PREF_REQUEST_LAUNCH_TIMEOUT);		
		MIProcess pgdb = new MIProcessAdapter(args, launchTimeout, monitor);
		
		if (MIPlugin.getDefault().isDebugging()) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < args.length; ++i) {
				sb.append(args[i]);
				sb.append(' ');
			}
			MIPlugin.getDefault().debugLog(sb.toString());
		}
		
		MISession session;
		try {
			session = createMISession(pgdb, null, MISession.ATTACH, miVersion, monitor);
		} catch (MIException e) {
			pgdb.destroy();
			throw e;
		}
		CommandFactory factory = session.getCommandFactory();
		try {
			if (targetParams != null && targetParams.length > 0) {
				MITargetSelect target = factory.createMITargetSelect(targetParams);
				session.postCommand(target);
				MIInfo info = target.getMIInfo();
				if (info == null) {
					throw new MIException(getResourceString("src.common.No_answer")); //$NON-NLS-1$
				}
			}
			if (pid > 0) {
				CLITargetAttach attach = factory.createCLITargetAttach(pid);
				session.postCommand(attach);
				MIInfo info = attach.getMIInfo();
				if (info == null) {
					throw new MIException(getResourceString("src.common.No_answer")); //$NON-NLS-1$
				}
				session.getMIInferior().setInferiorPID(pid);
				// @@@ for attach we nee to manually set the connected state
				// attach does not send the ^connected ack
				session.getMIInferior().setConnected();
			}
		} catch (MIException e) {
			pgdb.destroy();
			throw e;
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
		return getDefault().getBundle().getSymbolicName();
	}

	public void debugLog(String message) {
		if (getDefault().isDebugging()) {			
			// Time stamp
			message = MessageFormat.format( "[{0}] {1}", new Object[] { new Long( System.currentTimeMillis() ), message } ); //$NON-NLS-1$
			// This is to verbose for a log file, better use the console.
			//	getDefault().getLog().log(StatusUtil.newStatus(Status.ERROR, message, null));
			// ALERT:FIXME: For example for big buffers say 4k length,
			// the console will simply blows taking down eclipse.
			// This seems only to happen in Eclipse-gtk and Eclipse-motif
			// on GNU/Linux, so we break the lines in smaller chunks.
			while (message.length() > 100) {
				String partial = message.substring(0, 100);
				message = message.substring(100);
				System.err.println(partial + "\\"); //$NON-NLS-1$
			}
			if (message.endsWith("\n")) { //$NON-NLS-1$
				System.err.print(message);
			} else {
				System.err.println(message);
			}
		}
	}
	public static String getResourceString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		} catch (NullPointerException e) {
			return '#' + key + '#';
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#startup()
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#shutdown()
	 */
	public void stop(BundleContext context) throws Exception {
		savePluginPreferences();
		super.stop(context);
	}

	public static String getMIVersion( ILaunchConfiguration config ) {
		String miVersion = ""; //$NON-NLS-1$
		try {
			miVersion = config.getAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_PROTOCOL, "" ); //$NON-NLS-1$
		}
		catch( CoreException e ) {
		}
		if ( miVersion.length() == 0 ) {
			try {
				miVersion = config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_PROTOCOL, "mi" ); //$NON-NLS-1$
			}
			catch( CoreException e ) {
				miVersion = "mi"; //$NON-NLS-1$
			}
		}
		return miVersion;
	}
}
