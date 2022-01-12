/*******************************************************************************
 * Copyright (c) 2009, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.dsf.debug.ui.actions.AbstractDisassemblyBreakpointsTarget;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Toggle breakpoint target implementation for the disassembly part.
 */
public class DisassemblyToggleBreakpointsTarget extends AbstractDisassemblyBreakpointsTarget {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.AbstractDisassemblyBreakpointsTarget#createLineBreakpoint(java.lang.String, org.eclipse.core.resources.IResource, int)
	 */
	@Override
	protected void createLineBreakpoint(String sourceHandle, IResource resource, int lineNumber) throws CoreException {
		CDIDebugModel.createLineBreakpoint(sourceHandle, resource, getBreakpointType(), lineNumber, true, 0, "", true); //$NON-NLS-1$
	}

	@Override
	protected void createLineBreakpointInteractive(IWorkbenchPart part, String sourceHandle, IResource resource,
			int lineNumber) throws CoreException {
		ICLineBreakpoint lineBp = CDIDebugModel.createBlankLineBreakpoint();
		Map<String, Object> attributes = new HashMap<>();
		CDIDebugModel.setLineBreakpointAttributes(attributes, sourceHandle, getBreakpointType(), lineNumber, true, 0,
				""); //$NON-NLS-1$
		openBreakpointPropertiesDialog(lineBp, part, resource, attributes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.AbstractDisassemblyBreakpointsTarget#createAddressBreakpoint(org.eclipse.core.resources.IResource, org.eclipse.cdt.core.IAddress)
	 */
	@Override
	protected void createAddressBreakpoint(IResource resource, IAddress address) throws CoreException {
		CDIDebugModel.createAddressBreakpoint(null, null, resource, getBreakpointType(), address, true, 0, "", true); //$NON-NLS-1$
	}

	@Override
	protected void createAddressBreakpointInteractive(IWorkbenchPart part, IResource resource, IAddress address)
			throws CoreException {
		ICLineBreakpoint lineBp = CDIDebugModel.createBlankAddressBreakpoint();
		Map<String, Object> attributes = new HashMap<>();
		CDIDebugModel.setAddressBreakpointAttributes(attributes, null, null, getBreakpointType(), -1, address, true, 0,
				""); //$NON-NLS-1$
		openBreakpointPropertiesDialog(lineBp, part, resource, attributes);
	}

	protected int getBreakpointType() {
		return ICBreakpointType.REGULAR;
	}
}