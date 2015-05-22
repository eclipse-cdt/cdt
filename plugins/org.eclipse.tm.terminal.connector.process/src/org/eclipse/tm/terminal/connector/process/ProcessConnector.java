/*******************************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.process;

import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.tm.internal.terminal.emulator.VT100Emulator;
import org.eclipse.tm.internal.terminal.emulator.VT100TerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.NullSettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.terminal.connector.process.nls.Messages;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ILineSeparatorConstants;
import org.eclipse.tm.terminal.view.core.utils.Env;
import org.eclipse.tm.terminal.view.ui.manager.ConsoleManager;
import org.eclipse.tm.terminal.view.ui.streams.AbstractStreamsConnector;

/**
 * Process connector implementation.
 */
@SuppressWarnings("restriction")
public class ProcessConnector extends AbstractStreamsConnector {
	// Reference to the process settings
	private final ProcessSettings settings;

	// Reference to the PTY instance.
	private PTY pty;
	// Reference to the launched process instance.
	private Process process;
	// Reference to the process monitor
	private ProcessMonitor monitor;

	// The terminal width and height. Initially unknown.
	private int width = -1;
	private int height = -1;

	/**
	 * Constructor.
	 */
	public ProcessConnector() {
		this(new ProcessSettings());
	}

	/**
	 * Constructor.
	 *
	 * @param settings The process settings. Must not be <code>null</code>
	 */
	public ProcessConnector(ProcessSettings settings) {
		super();

		Assert.isNotNull(settings);
		this.settings = settings;
	}

	/**
	 * Returns the process object or <code>null</code> if the
	 * connector is connector.
	 *
	 * @return The process object or <code>null</code>.
	 */
	public Process getProcess() {
		return process;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#connect(org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl)
	 */
	@Override
	public void connect(ITerminalControl control) {
		Assert.isNotNull(control);
		super.connect(control);

		pty = null;
		width = -1;
		height = -1;

		try {
			boolean isAnsiTerminal = false;

			// Try to determine process and PTY instance from the process settings
			process = settings.getProcess();
			pty = settings.getPTY();

			// No process -> create PTY on supported platforms and execute
			// process image.
			if (process == null) {
				if (PTY.isSupported(PTY.Mode.TERMINAL)) {
					try {
						pty = new PTY(PTY.Mode.TERMINAL);

						// Initialize the terminal size
						VT100Emulator text = ((VT100TerminalControl)control).getTerminalText();
						text.fontChanged();
					} catch (IOException e) {
						// PTY not supported
					}
				}

				// Build up the command
				StringBuilder command = new StringBuilder(settings.getImage());
				String arguments = settings.getArguments();
				if (arguments != null && !"".equals(arguments.trim())) { //$NON-NLS-1$
					// Append to the command now
					command.append(" "); //$NON-NLS-1$
					command.append(arguments.trim());
				}

				File workingDir =null;
				if (settings.getWorkingDir()!=null){
					workingDir = new File(settings.getWorkingDir());
				}

				String[] envp = null;
				if (settings.getEnvironment()!=null){
					envp = settings.getEnvironment();
				}

				if (settings.isMergeWithNativeEnvironment()) {
					envp = Env.getEnvironment(envp, true);
				}

				isAnsiTerminal = getTermVariable(envp).startsWith("ansi"); //$NON-NLS-1$

				if (pty != null) {
					// A PTY is available -> can use the ProcessFactory.

					// Tokenize the command (ProcessFactory takes an array)
					StreamTokenizer st = new StreamTokenizer(new StringReader(command.toString()));
					st.resetSyntax();
					st.whitespaceChars(0, 32);
					st.whitespaceChars(0xa0, 0xa0);
					st.wordChars(33, 255);
					st.quoteChar('"');
					st.quoteChar('\'');

					List<String> argv = new ArrayList<String>();
					int ttype = st.nextToken();
					while (ttype != StreamTokenizer.TT_EOF) {
						argv.add(st.sval);
						ttype = st.nextToken();
					}

					// Execute the process
					process = ProcessFactory.getFactory().exec(argv.toArray(new String[argv.size()]), envp, workingDir, pty);
				} else {
					// No PTY -> just execute via the standard Java Runtime implementation.
					process = Runtime.getRuntime().exec(command.toString(), envp, workingDir);
				}
			}

			String lineSeparator = settings.getLineSeparator();
			if (lineSeparator == null && pty == null) {
				lineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$
				if ("\r".equals(lineSeparator)) { //$NON-NLS-1$
					lineSeparator = ILineSeparatorConstants.LINE_SEPARATOR_CR;
				}
				else if ("\n".equals(lineSeparator)) { //$NON-NLS-1$
					lineSeparator = ILineSeparatorConstants.LINE_SEPARATOR_LF;
				}
				else {
					lineSeparator = ILineSeparatorConstants.LINE_SEPARATOR_CRLF;
				}
			}

			// Setup the listeners
			setStdoutListeners(settings.getStdOutListeners());
			setStderrListeners(settings.getStdErrListeners());

			// Enable VT100 line wrapping if we are connected via pty
			// And TERM is VT100 compatible
			if (pty != null && !isAnsiTerminal)
				control.setVT100LineWrapping(true);

			// connect the streams
			connectStreams(control, process.getOutputStream(), process.getInputStream(), (pty == null ? process.getErrorStream() : null), settings.isLocalEcho(), lineSeparator);

			// Set the terminal control state to CONNECTED
			control.setState(TerminalState.CONNECTED);

			// Create the process monitor
			monitor = new ProcessMonitor(this);
			monitor.startMonitoring();
		} catch (IOException e) {
			// Disconnect right away
			disconnect();
			// Lookup the tab item
			CTabItem item = ConsoleManager.getInstance().findConsole(control);
			if (item != null) item.dispose();
			// Get the error message from the exception
			String msg = e.getLocalizedMessage() != null ? e.getLocalizedMessage() : ""; //$NON-NLS-1$
			Assert.isNotNull(msg);
			// Strip away "Exec_tty error:"
			msg = msg.replace("Exec_tty error:", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
			// Repackage into a more user friendly error
			msg = NLS.bind(Messages.ProcessConnector_error_creatingProcess, settings.getImage(), msg);
			// Open an error dialog
			MessageDialog.openError(control.getShell(), Messages.ProcessConnector_error_title, msg);
		}
	}

	private static String getTermVariable(String[] envp) {
		if (envp != null && !Platform.OS_WIN32.equals(Platform.getOS()))
			for (String var : envp)
		        if (var.startsWith("TERM=")) //$NON-NLS-1$
		        	return var.substring(5);
	    return "xterm"; //$NON-NLS-1$
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#isLocalEcho()
	 */
	@Override
	public boolean isLocalEcho() {
		return settings.isLocalEcho();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.streams.AbstractStreamsConnector#doDisconnect()
	 */
	@Override
	public void doDisconnect() {
		// Stop monitoring the process
		monitor.dispose();

		boolean isWindows = Platform.OS_WIN32.equals(Platform.getOS());

		if (!isWindows) {
			// Destroy the process first, except on windows (Bug 465674)
			if (process != null) { process.destroy(); process = null; }
		}

		// Dispose the streams
		super.doDisconnect();

		if (isWindows) {
			// On Windows destroy the process after closing streams
			if (process != null) { process.destroy(); process = null; }
		}

		// Set the terminal control state to CLOSED.
		fControl.setState(TerminalState.CLOSED);
	}

	// ***** Process Connector settings handling *****

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#setDefaultSettings()
	 */
	@Override
	public void setDefaultSettings() {
	    settings.load(new NullSettingsStore());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#getSettingsSummary()
	 */
	@Override
	public String getSettingsSummary() {
		return settings.getImage() != null ? settings.getImage() : ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#load(org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore)
	 */
	@Override
	public void load(ISettingsStore store) {
		settings.load(store);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#save(org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore)
	 */
	@Override
	public void save(ISettingsStore store) {
		settings.save(store);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl#setTerminalSize(int, int)
	 */
	@Override
	public void setTerminalSize(int newWidth, int newHeight) {
		if (width != newWidth || height != newHeight) {
			width = newWidth;
			height = newHeight;
			if (pty != null) {
				pty.setTerminalSize(newWidth, newHeight);
			}
		}
	}

}
