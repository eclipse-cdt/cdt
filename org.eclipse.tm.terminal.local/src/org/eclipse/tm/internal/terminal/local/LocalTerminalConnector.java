/***************************************************************************************************
 * Copyright (c) 2008, 2010 Mirko Raner and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mirko Raner - [196337] initial implementation; some methods adapted from
 *               org.eclipse.tm.terminal.ssh/SshConnector
 **************************************************************************************************/

package org.eclipse.tm.internal.terminal.local;

import java.io.OutputStream;
import java.text.Format;
import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.internal.terminal.local.launch.LocalTerminalLaunchUtilities;
import org.eclipse.tm.internal.terminal.local.process.LocalTerminalProcessFactory;
import org.eclipse.tm.internal.terminal.local.process.LocalTerminalProcessRegistry;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl;

/**
 * The class {@link LocalTerminalConnector} provides a terminal connector implementation for
 * connecting to local programs (for example, a locally running <code>bash</code> shell or
 * <code>vi</code> editor).
 *
 * @author Mirko Raner
 * @version $Revision: 1.3 $
 */
public class LocalTerminalConnector extends TerminalConnectorImpl
implements IDebugEventSetListener {

	// Shorthand for attribute names:
	//
	private final static String ATTR_CAPTURE_IN_CONSOLE = IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE;
	private final static String ATTR_CAPTURE_OUTPUT = DebugPlugin.ATTR_CAPTURE_OUTPUT;
	private final static String ATTR_PROCESS_FACTORY_ID = DebugPlugin.ATTR_PROCESS_FACTORY_ID;

	private LocalTerminalOutputStream terminalToLocalProcessStream;
	private LocalTerminalOutputListener outputListener;
	private LocalTerminalOutputListener errorListener;
	private ILocalTerminalSettings settings;
	private IStreamMonitor outputMonitor;
	private IStreamMonitor errorMonitor;
	private IProcess process;
	private ILaunch launch;

	/**
	 * Creates a new {@link LocalTerminalConnector}. This constructor is invoked by the framework.
	 */
	public LocalTerminalConnector() {

		settings = new LocalTerminalSettings();
	}

	/**
	 * Loads the connector's settings from the specified store.
	 *
	 * @param store the {@link ISettingsStore}
	 *
	 * @see TerminalConnectorImpl#load(ISettingsStore)
	 *
	 * TODO: the load(ISettingsStore) method should probably be made abstract in
	 *       TerminalConnectorImpl, otherwise it is not immediately clear that clients need to
	 *       override this method if custom settings are necessary (which they usually are).
	 *       Maybe the whole settings store mechanism should be redesigned. The current scheme
	 *       requires clients to implement load/save methods in their connector implementation
	 *       classes (necessity to override is not immediately obvious) and in the settings store
	 *       implementations (not enforced at all; merely expected by convention). Structurally,
	 *       all client implementations look more or less the same, and probably could be handled
	 *       by the framework in a uniform way. Maybe a configuration mechanism using attributes
	 *       (like, for example, ILaunchConfiguration) might be beneficial here.
	 */
	public void load(ISettingsStore store) {

		settings.load(store);
	}

	/**
	 * Stores the connector's settings into the specified store.
	 * See {@link #load(ISettingsStore)} for additional notes.
	 *
	 * @param store the {@link ISettingsStore}
	 *
	 * @see TerminalConnectorImpl#save(ISettingsStore)
	 */
	public void save(ISettingsStore store) {

		settings.save(store);
	}

	/**
	 * Creates the {@link ISettingsPage} for the settings of this connector.
	 *
	 * @return a new page that can be used in a dialog to setup this connection, or
	 * <code>null</code> if the connection cannot be customized or configured
	 *
	 * @see TerminalConnectorImpl#makeSettingsPage()
	 */
	public ISettingsPage makeSettingsPage() {

		return new LocalTerminalSettingsPage(settings);
	}

	/**
	 * Returns a string that represents the settings of the connection.
	 *
	 * @return the name of the launch configuration that is running in the terminal
	 *
	 * @see TerminalConnectorImpl#getSettingsSummary()
	 * @see ILocalTerminalSettings#getLaunchConfigurationName()
	 */
	public String getSettingsSummary() {

		return settings.getLaunchConfigurationName();
	}

	/**
	 * Checks if local echo is required.
	 *
	 * @return <code>true</code> if the connection settings specify that local echo is enable,
	 * <code>false</code> otherwise
	 *
	 * @see TerminalConnectorImpl#isLocalEcho()
	 * @see LocalTerminalLaunchUtilities#ATTR_LOCAL_ECHO
	 */
	public boolean isLocalEcho() {

		return LocalTerminalUtilities.getLocalEcho(settings);
	}

    /**
     * Returns an {@link OutputStream} that writes to the local program's standard input. For the
     * stream in the other direction (remote to terminal) see
     * {@link ITerminalControl#getRemoteToTerminalOutputStream()}.
     *
     * @return the terminal-to-remote-stream (bytes written to this stream will be sent to the
     * local program)
     */
	public OutputStream getTerminalToRemoteStream() {

		return terminalToLocalProcessStream;
	}

	/**
	 * Connects a locally launched program to the {@link ITerminalControl}.
	 *
	 * @param control the {@link ITerminalControl} through which the user can interact with the
	 * program
	 */
	public void connect(ITerminalControl control) {

		super.connect(control);
		control.setState(TerminalState.CONNECTING);
		ILaunchConfigurationWorkingCopy workingCopy = null;
		ILaunchConfiguration configuration = null;
		String configurationName = null;
		try {

			configurationName = settings.getLaunchConfigurationName();
			configuration = LocalTerminalUtilities.findLaunchConfiguration(configurationName);
		}
		catch (CoreException exception) {

			Shell shell = Display.getDefault().getActiveShell();
			String title = LocalTerminalMessages.errorTitleCouldNotConnectToTerminal;
			Format text;
			text = new MessageFormat(LocalTerminalMessages.errorLaunchConfigurationNoLongerExists);
			String message = text.format(new Object[] {configurationName});
			IStatus status = new Status(IStatus.ERROR, LocalTerminalActivator.PLUGIN_ID, message);
			ErrorDialog.openError(shell, title, null, status);
			control.setState(TerminalState.CLOSED);
			return;
		}
		try {

			String oldFactoryID = configuration.getAttribute(ATTR_PROCESS_FACTORY_ID, (String)null);
			workingCopy = configuration.getWorkingCopy();
			workingCopy.setAttribute(ATTR_CAPTURE_OUTPUT, true);
			workingCopy.setAttribute(ATTR_CAPTURE_IN_CONSOLE, true);
			workingCopy.setAttribute(ATTR_PROCESS_FACTORY_ID, LocalTerminalProcessFactory.ID);
			configuration = workingCopy.doSave();
			try {

				launch = configuration.launch(ILaunchManager.RUN_MODE, null);
			}
			finally {

				// The process factory ID is used to distinguish between launches that originate
				// from the terminal connector and launches that originate from the launch dialog.
				// After launching, the original ID is restored so that the launch is not mistaken
				// as originating from the terminal connector UI when it is launched via the launch
				// dialog the next time:
				//
				workingCopy = configuration.getWorkingCopy();
				workingCopy.setAttribute(ATTR_PROCESS_FACTORY_ID, oldFactoryID);
				workingCopy.doSave();
			}

			// To prevent a console from being allocated, the launch will actually not contain a
			// reference to the runtime process. The process has to be obtained from the
			// LocalTerminalProcessRegistry instead:
			//
			process = LocalTerminalProcessRegistry.getFromLaunch(launch);
			IStreamsProxy streamsProxy = process.getStreamsProxy();

			// Hook up standard input:
			//
			terminalToLocalProcessStream = new LocalTerminalOutputStream(process, settings);

			// Hook up standard output:
			//
			outputMonitor = streamsProxy.getOutputStreamMonitor();
			outputListener = new LocalTerminalOutputListener(control, settings);
			outputMonitor.addListener(outputListener);
			outputListener.streamAppended(outputMonitor.getContents(), outputMonitor);

			// Hook up standard error:
			//
			errorMonitor = streamsProxy.getErrorStreamMonitor();
			errorListener = new LocalTerminalOutputListener(control, settings);
			errorMonitor.addListener(errorListener);
			errorListener.streamAppended(errorMonitor.getContents(), errorMonitor);
			//
			// TODO: add proper synchronization for incoming data from stdout and stderr:
			//       currently, the data gets sometimes processed in the wrong order, for example,
			//       the next prompt (which shells like bash print to stderr) sometimes appears
			//       before the command's proper output that was sent to stdout. For example,
			//       you get:
			//
			//       $ echo hello
			//       $ hello
			//
			//       instead of the correct output of:
			//
			//       $ echo hello
			//       hello
			//       $

			// Listen for process termination and update the terminal state:
			//
			DebugPlugin.getDefault().addDebugEventListener(this);
			control.setState(TerminalState.CONNECTED);
		}
		catch (CoreException exception) {

			control.setState(TerminalState.CLOSED);
			Shell shell = LocalTerminalSettingsPage.getShell();
			ErrorDialog.openError(shell, null, null, exception.getStatus());
			Logger.logException(exception);
		}
	}

	/**
	 * Disconnects the connector if it is currently connected or does nothing otherwise. This method
	 * will try to terminate the underlying launched process and will remove all registered
	 * listeners.
	 */
	public void doDisconnect() {

		try {

			removeAllListeners();

			// To prevent a console from being allocated, Terminal launches don't have an IProcess
			// associated with them while they are running. However, to properly terminate a launch
			// the launch has to contain at least one process that can be terminated (launches
			// without processes effectively cannot be terminated):
			//
			LocalTerminalProcessRegistry.addProcessBackToFinishedLaunch(launch);

			// Now, terminate the process if it was ever started and hasn't been terminated already:
			//
			if (launch != null && launch.canTerminate()) {

				launch.terminate();
				//
				// NOTE: canTerminate() merely indicates that the launch has not been terminated
				//       previously already
			}
		}
		catch (DebugException couldNotTerminate) {

			Logger.logException(couldNotTerminate);
		}
	}

	/**
	 * Listens for self-induced termination of the launched process. For example, this method will
	 * be notified if a launched shell is terminated by pressing Control-D or by calling
	 * <code>exit</code>, or if a <code>vi</code> editor is terminated by means of a
	 * <code>:q!</code> command.
	 *
	 * @param event the debug events
	 *
	 * @see IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] event) {

		int numberOfEvents = event.length;
		for (int index = 0; index < numberOfEvents; index++) {

			if (event[index].getSource().equals(process)
			&& (event[index].getKind() == DebugEvent.TERMINATE)) {

				fControl.setState(TerminalState.CLOSED);
				removeAllListeners();
				return;
			}
		}
	}

	/**
	 * Removes any listeners that the {@link LocalTerminalConnector} might have registered in its
	 * {@link #connect(ITerminalControl)} method. This method is necessary for clean-up when a
	 * connection is closed. It prevents that orphaned or meaningless listeners keep accumulating
	 * on certain objects.
	 */
	protected void removeAllListeners() {

		if (outputMonitor != null && outputListener != null) {

			outputMonitor.removeListener(outputListener);
		}
		if (errorMonitor != null && errorListener != null) {

			errorMonitor.removeListener(errorListener);
		}
		DebugPlugin.getDefault().removeDebugEventListener(this);
	}
}
