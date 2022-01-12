/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.commands;

import org.eclipse.cdt.debug.core.model.IResumeWithoutSignal;
import org.eclipse.cdt.debug.core.model.IResumeWithoutSignalHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.commands.AbstractDebugCommand;
import org.eclipse.debug.core.commands.IEnabledStateRequest;

/**
 * The handler for the "Resume Without Signal" command.
 *
 * @since 7.0
 */
public class ResumeWithoutSignalCommand extends AbstractDebugCommand implements IResumeWithoutSignalHandler {

	@Override
	protected void doExecute(Object[] targets, IProgressMonitor monitor, IRequest request) throws CoreException {
		if (targets.length != 1) {
			return;
		}

		IResumeWithoutSignal target = (IResumeWithoutSignal) targets[0];
		target.resumeWithoutSignal();
	}

	@Override
	protected boolean isExecutable(Object[] targets, IProgressMonitor monitor, IEnabledStateRequest request)
			throws CoreException {
		if (targets.length != 1) {
			return false;
		}

		IResumeWithoutSignal target = (IResumeWithoutSignal) targets[0];
		return target.canResumeWithoutSignal();
	}

	@Override
	protected Object getTarget(Object element) {
		return DebugPlugin.getAdapter(element, IResumeWithoutSignal.class);
	}
}
