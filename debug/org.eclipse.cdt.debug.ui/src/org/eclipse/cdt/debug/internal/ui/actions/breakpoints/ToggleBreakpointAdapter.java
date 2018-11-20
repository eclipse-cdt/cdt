/*******************************************************************************
 * Copyright (c) 2004, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Freescale Semiconductor - Address watchpoints, https://bugs.eclipse.org/bugs/show_bug.cgi?id=118299
 * Warren Paul (Nokia) - Bug 217485, Bug 218342
 * Oyvind Harboe (oyvind.harboe@zylin.com) - Bug 225099
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions.breakpoints;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.core.model.ICEventBreakpoint;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.ui.breakpoints.AbstractToggleBreakpointAdapter;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Toggles a line breakpoint in a C/C++ editor.
 *
 * @since 7.2
 */
public class ToggleBreakpointAdapter extends AbstractToggleBreakpointAdapter {

	@Override
	protected ICLineBreakpoint findLineBreakpoint(String sourceHandle, IResource resource, int lineNumber)
			throws CoreException {
		return CDIDebugModel.lineBreakpointExists(sourceHandle, resource, lineNumber);
	}

	@Override
	protected void createLineBreakpoint(boolean interactive, IWorkbenchPart part, String sourceHandle,
			IResource resource, int lineNumber) throws CoreException {
		if (interactive) {
			ICLineBreakpoint lineBp = CDIDebugModel.createBlankLineBreakpoint();
			Map<String, Object> attributes = new HashMap<>();
			CDIDebugModel.setLineBreakpointAttributes(attributes, sourceHandle, getBreakpointType(), lineNumber, true,
					0, ""); //$NON-NLS-1$
			openBreakpointPropertiesDialog(lineBp, part, resource, attributes);
		} else {
			CDIDebugModel.createLineBreakpoint(sourceHandle, resource, getBreakpointType(), lineNumber, true, 0, "", //$NON-NLS-1$
					true);
		}
	}

	@Override
	protected ICFunctionBreakpoint findFunctionBreakpoint(String sourceHandle, IResource resource, String functionName)
			throws CoreException {
		return CDIDebugModel.functionBreakpointExists(sourceHandle, resource, functionName);
	}

	@Override
	protected void createFunctionBreakpoint(boolean interactive, IWorkbenchPart part, String sourceHandle,
			IResource resource, String functionName, int charStart, int charEnd, int lineNumber) throws CoreException {
		if (interactive) {
			ICFunctionBreakpoint bp = CDIDebugModel.createBlankFunctionBreakpoint();
			Map<String, Object> attributes = new HashMap<>();
			CDIDebugModel.setFunctionBreakpointAttributes(attributes, sourceHandle, getBreakpointType(), functionName,
					charStart, charEnd, lineNumber, true, 0, ""); //$NON-NLS-1$
			openBreakpointPropertiesDialog(bp, part, resource, attributes);
		} else {
			CDIDebugModel.createFunctionBreakpoint(sourceHandle, resource, getBreakpointType(), functionName, charStart,
					charEnd, lineNumber, true, 0, "", true); //$NON-NLS-1$
		}
	}

	@Override
	protected ICWatchpoint findWatchpoint(String sourceHandle, IResource resource, String expression)
			throws CoreException {
		return CDIDebugModel.watchpointExists(sourceHandle, resource, expression);
	}

	@Override
	protected void createWatchpoint(boolean interactive, IWorkbenchPart part, String sourceHandle, IResource resource,
			int charStart, int charEnd, int lineNumber, String expression, String memorySpace, String range)
			throws CoreException {
		ICWatchpoint bp = CDIDebugModel.createBlankWatchpoint();
		Map<String, Object> attributes = new HashMap<>();
		CDIDebugModel.setWatchPointAttributes(attributes, sourceHandle, resource, true, false, expression, memorySpace,
				new BigInteger(range), true, 0, ""); //$NON-NLS-1$
		openBreakpointPropertiesDialog(bp, part, resource, attributes);
	}

	@Override
	protected void createEventBreakpoint(boolean interactive, IWorkbenchPart part, IResource resource, String type,
			String arg) throws CoreException {
		ICEventBreakpoint bp = CDIDebugModel.createBlankEventBreakpoint();
		Map<String, Object> attributes = new HashMap<>();
		CDIDebugModel.setEventBreakpointAttributes(attributes, type, arg);
		openBreakpointPropertiesDialog(bp, part, resource, attributes);
	}

	protected int getBreakpointType() {
		return ICBreakpointType.REGULAR;
	}
}
