/*******************************************************************************
 * Copyright (c) 2016, 2022 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Dallaway - Eliminate deprecated method (#112)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.cdt.core.parser.util.StringUtil;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.pty.PTY.Mode;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.osgi.util.NLS;

/**
 * Implementation of {@link IGDBBackend} using GDB 7.12. This version provides
 * full GDB console support.  It achieves this by launching GDB in CLI mode
 * in a special console widget and then connecting to GDB via MI by telling GDB to
 * open a new MI console.  The rest of the DSF-GDB support then stays the same.
 *
 * If we are unable to create a PTY, we then revert to the previous behavior of
 * the base class.
 *
 * @since 5.2
 */
public class GDBBackend_7_12 extends GDBBackend {

	/** The PTY that is used to create the MI channel */
	private PTY fMIPty;
	/** The PTY that is used to create the GDB process in CLI mode */
	private PTY fCLIPty;

	/** Indicate that we failed to create a PTY. */
	private boolean fPtyFailure;

	private InputStream fDummyErrorStream;

	public GDBBackend_7_12(DsfSession session, ILaunchConfiguration lc) {
		super(session, lc);
		createPty();
	}

	@Override
	public boolean isFullGdbConsoleSupported() {
		return System.getenv("FLATPAK_SANDBOX_DIR") == null //$NON-NLS-1$
				&& !Platform.getOS().equals(Platform.OS_WIN32) && !Platform.getOS().equals(Platform.OS_MACOSX)
				&& !fPtyFailure;
	}

	/**
	 * @since 6.2
	 */
	@Override
	public boolean useTargetAsync() {
		// Enable target asynchronously if there is Full GDB console as Full GDB Console requires async target or
		// If Windows remote debugging as remote debugging in GDB has lots of issues with handling Ctrl-C (See Bug 516371)
		return isFullGdbConsoleSupported()
				|| (Platform.getOS().equals(Platform.OS_WIN32) && getSessionType() == SessionType.REMOTE);
	}

	protected void createPty() {
		if (!isFullGdbConsoleSupported()) {
			return;
		}

		try {
			fMIPty = new PTY();
			fMIPty.validateSlaveName();

			// With the PTY the stderr is redirected to the PTY's output stream.
			// Therefore, return a dummy stream for the error stream.
			fDummyErrorStream = new InputStream() {
				@Override
				public int read() throws IOException {
					return -1;
				}
			};
		} catch (IOException e) {
			fMIPty = null;
			fPtyFailure = true;
			GdbPlugin.log(new Status(IStatus.INFO, GdbPlugin.PLUGIN_ID,
					NLS.bind(Messages.PTY_Console_not_available, e.getMessage())));
		}
	}

	@Override
	public OutputStream getMIOutputStream() {
		if (fMIPty == null) {
			return super.getMIOutputStream();
		}
		return fMIPty.getOutputStream();
	}

	@Override
	public InputStream getMIInputStream() {
		if (fMIPty == null) {
			return super.getMIInputStream();
		}
		return fMIPty.getInputStream();
	}

	@Override
	public InputStream getMIErrorStream() {
		if (fMIPty == null) {
			return super.getMIErrorStream();
		}
		return fDummyErrorStream;
	}

	@Override
	public String[] getDebuggerCommandLineArray() {
		// Start from the original command line method and add what we need
		// to convert it to a command that will launch in CLI mode.
		// Then trigger the MI console
		String[] originalCommandLine = super.getDebuggerCommandLineArray();

		if (!isFullGdbConsoleSupported()) {
			return originalCommandLine;
		}

		// Below are the parameters we need to add to an existing commandLine,
		// to trigger a launch with the full CLI.  This would also work
		// as the only parameters for a full CLI launch (although "--interpreter console"
		// could be removed in that case)
		String[] extraArguments = new String[] {
				// Start with -q option to avoid extra output which may trigger pagination
				// This is important because if pagination is triggered on the version
				// printout, we won't be able to send the command to start the MI channel.
				// Note that we cannot turn off pagination early enough to prevent the
				// original version output from paginating
				"-q", //$NON-NLS-1$

				// We don't put --nx at this time because our base class puts it already and if
				// if an extender has removed it, we shouldn't add it again.
				// Once we no longer extends the deprecated getGDBCommandLineArray() and simply
				// create the full commandLine here, we should put it
				//				// Use the --nx option to avoid reading the gdbinit file here.
				//				// The gdbinit file is read explicitly in the FinalLaunchSequence to make
				//				// it easier to customize.
				//				"--nx", //$NON-NLS-1$

				// Force a CLI console since the originalCommandLine
				// probably specified "-i mi" or "--interpreter mi"
				// Once we no longer extend the deprecated
				// getGDBCommandLineArray() and simply create the full
				// commandLine here, we could remove this parameter
				"--interpreter", "console", //$NON-NLS-1$ //$NON-NLS-2$

				// Now trigger the new console towards our PTY.
				"-ex", "new-ui mi " + fMIPty.getSlaveName(), //$NON-NLS-1$ //$NON-NLS-2$

				// With GDB.7.12, pagination can lock up the whole debug session
				// when using the full GDB console, so we turn it off.
				// We must turn it off before calling 'show version' as even
				// that command could cause pagination to trigger
				"-ex", "set pagination off", //$NON-NLS-1$//$NON-NLS-2$

				// Now print the version so the user gets that familiar output
				"-ex", "show version" //$NON-NLS-1$ //$NON-NLS-2$
		};

		int oriLength = originalCommandLine.length;
		int extraLength = extraArguments.length;
		String[] newCommandLine = new String[oriLength + extraLength];
		System.arraycopy(originalCommandLine, 0, newCommandLine, 0, oriLength);
		System.arraycopy(extraArguments, 0, newCommandLine, oriLength, extraLength);

		return newCommandLine;
	}

	@Override
	protected Process launchGDBProcess() throws CoreException {
		if (!isFullGdbConsoleSupported()) {
			return super.launchGDBProcess();
		}

		// If we are launching the full console, we need to use a PTY in TERMINAL mode
		// for the GDB CLI to properly display in its view
		Process proc = null;
		String[] commandLine = getDebuggerCommandLineArray();
		try {
			fCLIPty = new PTY(Mode.TERMINAL);
			IPath path = getGDBWorkingDirectory();
			proc = ProcessFactory.getFactory().exec(commandLine, getGDBLaunch().getLaunchEnvironment(),
					new File(path != null ? path.toOSString() : ""), //$NON-NLS-1$
					fCLIPty);
		} catch (IOException e) {
			String message = "Error while launching command: " + StringUtil.join(commandLine, " "); //$NON-NLS-1$ //$NON-NLS-2$
			throw new CoreException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, message, e));
		}

		return proc;
	}

	@Override
	public PTY getProcessPty() {
		return fCLIPty;
	}
}
