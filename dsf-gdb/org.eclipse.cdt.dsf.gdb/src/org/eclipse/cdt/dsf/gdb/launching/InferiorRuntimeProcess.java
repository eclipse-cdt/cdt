/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Ericsson - Initial API and implementation
 *  Marc Khouzam (Ericsson) - Display exit code in process console (Bug 402054)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import java.util.Map;

import org.eclipse.cdt.dsf.gdb.IGdbDebugConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.RuntimeProcess;

import com.ibm.icu.text.MessageFormat;

/**
 * A process for the inferior to know it belongs to a DSF-GDB session
 * 
 * @since 4.0
 */
public class InferiorRuntimeProcess extends RuntimeProcess {
	
	public InferiorRuntimeProcess(ILaunch launch, Process process, String name,
			Map<String, String> attributes) {
		super(launch, process, name, attributes);
	}
	
	@Override
	protected void terminated() {
		super.terminated();
		setConsoleTerminatedLabel();
	}
	
	// Inspired from org.eclipse.debug.internal.ui.views.console.ProcessConsole#computeName
	// We set the IProcess.ATTR_PROCESS_LABEL to modify the console title but not the process label
	// of the debug view.  Overriding getLabel() affects the element in the debug view also, so
	// we don't do that.
	private void setConsoleTerminatedLabel() {
		String label = getLabel();

		String type = null;
		ILaunchConfiguration config = getLaunch().getLaunchConfiguration();
		try {
			type = config.getType().getName();
		} catch (CoreException e) {
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append(config.getName());
		if (type != null) {
			buffer.append(" ["); //$NON-NLS-1$
			buffer.append(type);
			buffer.append("] "); //$NON-NLS-1$
		}
		buffer.append(label);

		if (getAttribute(IGdbDebugConstants.INFERIOR_EXITED_ATTR) != null) {
			// Add the exit code to the label if the inferior properly exited.
			try {
				buffer.insert(0, MessageFormat.format(LaunchMessages.getString("InferiorRuntimeProcess_ExitValue"), //$NON-NLS-1$
						new Object[] { getExitValue() }));
			} catch (DebugException e) {
				// Process not terminated.  Should not happen.  But even so, we should use the plain label.
			}
		}
		label = buffer.toString();

		setAttribute(IProcess.ATTR_PROCESS_LABEL, label);
	}
}
