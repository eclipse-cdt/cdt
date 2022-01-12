/*******************************************************************************
 * Copyright (c) 2013, 2016 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.launch;

import java.util.ArrayList;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.ui.actions.ILaunchable;

/**
 * This is an invalid Adapter factory which insures that there  are no false
 * usages of this class when defining ILaunchable contexts. Please reference
 * the ILaunchable interface and Bugzilla : 396822.
 */
public class InvalidLaunchableAdapterFactory implements IAdapterFactory {
	private static final Class<?>[] TYPES = { ILaunchable.class };
	private static ArrayList<String> currentTraces = new ArrayList<>();

	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		/*
		 * Calculate the trace to see if we already have seen this one. We only
		 * want to report new instances of the violation.
		 */
		String trace = getStackTrace();

		if (!currentTraces.contains(trace)) {
			/*
			 * Note we have seen this one for the first time.
			 */
			currentTraces.add(trace);

			/*
			 * Generate a message for this in the log file.
			 */
			String msg = LaunchMessages.getString("Launch.ILaunchable.Interface.Error"); //$NON-NLS-1$

			CDebugUIPlugin.log(new Status(IStatus.INFO, CDebugUIPlugin.PLUGIN_ID, 0, msg, new Throwable(""))); //$NON-NLS-1$
		}

		// We do not actually provide an adapter factory for this.
		return null;
	}

	/*
	 * Constructs the stack trace for comparison to see if we have seen this exact trace before.
	 * We only report each unique instance once.
	 */
	private String getStackTrace() {
		String trace = ""; //$NON-NLS-1$
		for (StackTraceElement elem : new Throwable().getStackTrace()) {
			trace += elem.getClassName() + elem.getMethodName() + elem.getFileName() + elem.getLineNumber();
		}
		return trace;
	}

	/*
	 * Indicates that we are adapting ILaunchable.
	 */
	@Override
	public Class<?>[] getAdapterList() {
		return TYPES;
	}
}
