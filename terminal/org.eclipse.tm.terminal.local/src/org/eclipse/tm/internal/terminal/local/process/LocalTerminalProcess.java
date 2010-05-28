/***************************************************************************************************
 * Copyright (c) 2008, 2010 Mirko Raner.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mirko Raner - initial implementation for Eclipse Bug 196337
 * Mirko Raner - [314607] Launching a terminal also pops up the console view
 **************************************************************************************************/

package org.eclipse.tm.internal.terminal.local.process;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.Spawner;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.RuntimeProcess;

/**
 * The class {@link LocalTerminalProcess} is a customized {@link RuntimeProcess} for use by the
 * {@link org.eclipse.tm.internal.terminal.local.LocalTerminalConnector}. It serves the purpose of
 * preventing the {@link org.eclipse.debug.internal.ui.DebugUIPlugin DebugUIPlugin}'s
 * {@link org.eclipse.debug.internal.ui.views.console.ProcessConsoleManager ProcessConsoleManager}
 * from allocating a Console view in addition to the Terminal view that serves as the program's main
 * I/O device. <p>
 * Unfortunately, the Process Console Manager determines the need for a Console view by checking the
 * {@link IStreamsProxy} of the process for <code>null</code>. If the process has a non-null
 * {@link IStreamsProxy} a console will be automatically allocated. This is problematic because
 * the Local Terminal Connector requires an {@link IStreamsProxy} but obviously not an additional
 * console view. It would have been better if the Process Console Manager would check the
 * corresponding attributes in the launch configuration rather than checking the
 * {@link IStreamsProxy} of the process. The work-around for now is to remove the underlying
 * process from the launch. No console will be allocated for a launch that doesn't have a process
 * associated with it. Consequently, a currently running terminal launch will appear in the Debug
 * view's list of active launches but the view will not show any child elements (and the element
 * cannot be expanded, either). The {@link LocalTerminalProcessRegistry} keeps track of which
 * {@link LocalTerminalProcess} is associated with a particular launch. Client code that needs to
 * find the process of a launch can obtain it through that registry. <p>
 * However, for a launch to be properly terminated it needs to have at least one process that can
 * be terminated. Launches that do not have any processes associated with them are not considered
 * terminated and actually terminating them is not possible. To work around this secondary issue,
 * the process is added back to its launch just before the launch is terminated. This activity is
 * performed by {@link LocalTerminalProcessRegistry#addProcessBackToFinishedLaunch(ILaunch)}. To
 * prevent a console allocation during this last step, the {@link #resetStreamsProxy()} method will
 * be invoked, which will cause subsequent calls to {@link IProcess#getStreamsProxy()} to return
 * <code>null</code>. After the launch is terminated it will appear in the Debug view with the
 * terminated process as its child element. The exit value of the terminal process can also be seen
 * in that view. <p>
 * {@link #getStreamsProxy()} will also return <code>null</code> during initialization of a
 * {@link LocalTerminalProcess} object until after the
 * {@link RuntimeProcess#RuntimeProcess(ILaunch, Process, String, Map) super constructor} invocation
 * has been completed. This disables a code path that caused a Console view to pop up when the
 * {@link org.eclipse.debug.core.model.IStreamMonitor IStreamMonitor} already contained data (like,
 * for example, a shell's initial prompt) when the <code>ProcessConsoleManager</code> received an
 * {@link org.eclipse.debug.core.ILaunchListener#launchChanged(ILaunch)} notification (which cannot
 * be avoided). See <a href="https://bugs.eclipse.org/314607">https://bugs.eclipse.org/314607</a>
 * for additional details. <p/>
 *
 * This solution for preventing standard consoles from being opened does certainly not deserve the
 * cleanliness award for straightforward coding, but at least it doesn't rely on internal API at
 * this point. Ideally, the whole issue should be resolved with some sort of console renderer
 * extension point as proposed in bug 242373 (https://bugs.eclipse.org/bugs/show_bug.cgi?id=242373).
 *
 * @author Mirko Raner
 * @version $Revision: 1.2 $
 */
public final class LocalTerminalProcess extends RuntimeProcess {

	/**
	 * The process type ID of processes produced by this factory.
	 */
	public final static String PROCESS_TYPE = "org.eclipse.tm.terminal.localProcess"; //$NON-NLS-1$

	private boolean enableStreamsProxy;
	private boolean resetStreamsProxy;
	private PTY pty;

	/**
	 * Creates a new {@link LocalTerminalProcess}.
	 *
	 * @param launch the current {@link ILaunch}
	 * @param process the underlying low-level {@link Process}
	 * @param name the process name
	 * @param attributes additional attributes of the process
	 */
	protected LocalTerminalProcess(ILaunch launch, Process process, String name, Map attributes) {

		super(launch, process, name, setProcessType(attributes));
		enableStreamsProxy = true;
		LocalTerminalProcessRegistry.registerWithLaunch(launch, this);
		launch.removeProcess(this);
	}

	/**
	 * Sends a <code>SIGINT</code> signal to the underlying system {@link Process}. This is roughly
	 * equivalent to the user pressing Ctrl-C.
	 *
	 * @return <code>true</code> if the interrupt signal was sent successfully; <code>false</code>
	 * if the signal was not sent successfully or if no signal was sent because the underlying
	 * process is not a CDT {@link Spawner}
	 */
	public boolean interrupt() {

		Process process = getSystemProcess();
		if (process instanceof Spawner) {

			return ((Spawner)process).interrupt() == 0;
		}
		return false;
	}

	/**
	 * Returns the {@link IStreamsProxy} of the process.
	 *
	 * @return the original result of {@link RuntimeProcess#getStreamsProxy()}, or <code>null</code>
	 * after {@link #resetStreamsProxy()} has been called.
	 */
	public IStreamsProxy getStreamsProxy() {

		if (resetStreamsProxy || !enableStreamsProxy) {

			return null;
		}
		return super.getStreamsProxy();
	}

	/**
	 * Resets the {@link IStreamsProxy} of this process. After calling this method,
	 * {@link #getStreamsProxy()} will always return <code>null</code>.
	 */
	protected void resetStreamsProxy() {

		resetStreamsProxy = true;
	}

	/**
	 * Sets the pseudo-terminal associated with this process.
	 *
	 * @param pty the {@link PTY}
	 */
	public void setPTY(PTY pty) {

		this.pty = pty;
	}

	/**
	 * Gets the pseudo-terminal associated with this process.
	 *
	 * @return the {@link PTY}
	 */
	public PTY getPTY() {

		return pty;
	}

	/**
	 * Re-attaches the process to its launch and completes termination of the process. This ensures
	 * that the launch can properly terminate.
	 *
	 * @see RuntimeProcess#terminated()
	 */
	protected void terminated() {

		LocalTerminalProcessRegistry.addProcessBackToFinishedLaunch(getLaunch());
		super.terminated();
	}

	//------------------------------------- PRIVATE SECTION --------------------------------------//

	private static Map setProcessType(Map attributes) {

		// The process type used to be set by the LocalTerminalProcessFactory. However, if some
		// client code managed to instantiate a LocalTerminalProcess directly (instead of going
		// through the factory) this would result in IProcess objects with an incorrect process type
		// attribute. A better solution is to set the process type attribute at the time when the
		// LocalTerminalProcess object is actually created:
		//
		if (attributes == null) {

			attributes = new HashMap(1);
		}
        attributes.put(IProcess.ATTR_PROCESS_TYPE, PROCESS_TYPE);
        return attributes;
	}
}
