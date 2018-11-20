/*******************************************************************************
 * Copyright (c) 2011, 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	Ericsson - Initial API and implementation
 *  Marc Khouzam (Ericsson) - Display exit code in process console (Bug 402054)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import java.util.Map;

import org.eclipse.cdt.dsf.gdb.IGdbDebugConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.RuntimeProcess;

import com.ibm.icu.text.MessageFormat;

/**
 * A process for the inferior to know it belongs to a DSF-GDB session.
 * This class also adds the exit code of the inferior to the console.
 *
 * Note that this class is also used in Run mode.
 *
 * @since 4.0
 */
public class InferiorRuntimeProcess extends RuntimeProcess {

	public InferiorRuntimeProcess(ILaunch launch, Process process, String name, Map<String, String> attributes) {
		super(launch, process, name, attributes);
	}

	@Override
	protected void terminated() {
		// We must set the console label before calling super.terminated()
		// This is because super.terminated() will send an event to rename
		// the console, and we find ourselves in a race condition
		// where we may miss setting the label here (bug 463977)
		setConsoleTerminatedLabel();

		super.terminated();
	}

	private void setConsoleTerminatedLabel() {
		if (getAttribute(IGdbDebugConstants.INFERIOR_EXITED_ATTR) != null) {
			// Add the exit code to the title of the console if the inferior properly exited.
			int exitValue = 0;
			try {
				// We have to explicitly get the exit code from the lower level process
				// instead of calling getExitValue() because we have not yet indicated
				// that this wrapper process has terminated by calling super.terminated()
				// Bug 463977
				exitValue = getSystemProcess().exitValue();
			} catch (IllegalThreadStateException e) {
				// Process not terminated.  Should not happen.  Use default behavior.
				return;
			}

			// Inspired from org.eclipse.debug.internal.ui.views.console.ProcessConsole#computeName
			// We set the IProcess.ATTR_PROCESS_LABEL to modify the console title but not the process label
			// of the debug view.  Overriding getLabel() affects the element in the debug view also, so
			// we don't do that.
			String label = getLabel();

			String type = null;
			ILaunchConfiguration config = getLaunch().getLaunchConfiguration();
			try {
				type = config.getType().getName();
			} catch (CoreException e) {
				type = null;
			}
			StringBuilder buffer = new StringBuilder();
			buffer.append(config.getName());
			if (type != null) {
				buffer.append(" ["); //$NON-NLS-1$
				buffer.append(type);
				buffer.append("] "); //$NON-NLS-1$
			}
			buffer.append(label);

			// Prefix with exit value
			buffer.insert(0, MessageFormat.format(LaunchMessages.getString("InferiorRuntimeProcess_ExitValue"), //$NON-NLS-1$
					new Object[] { exitValue }));
			label = buffer.toString();

			setAttribute(IProcess.ATTR_PROCESS_LABEL, label);
		}
	}
}
