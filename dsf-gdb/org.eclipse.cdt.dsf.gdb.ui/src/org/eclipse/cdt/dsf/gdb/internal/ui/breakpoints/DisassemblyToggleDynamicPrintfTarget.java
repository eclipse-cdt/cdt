/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.breakpoints;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.core.model.ICDynamicPrintf;
import org.eclipse.cdt.dsf.debug.ui.actions.AbstractDisassemblyBreakpointsTarget;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Toggle dynamic printf target implementation for the disassembly part.
 */
public class DisassemblyToggleDynamicPrintfTarget extends AbstractDisassemblyBreakpointsTarget {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.AbstractDisassemblyBreakpointsTarget#createLineBreakpoint(java.lang.String, org.eclipse.core.resources.IResource, int)
	 */
	@Override
	protected void createLineBreakpoint(String sourceHandle, IResource resource, int lineNumber) throws CoreException {
		// We provide a default printf string to make the dynamic printf useful automatically
		String printfStr = NLS.bind(Messages.Default_LineDynamicPrintf_String, escapeBackslashes(sourceHandle),
				lineNumber);

		CDIDebugModel.createLineDynamicPrintf(sourceHandle, resource, getBreakpointType(), lineNumber, true, 0, "", //$NON-NLS-1$
				printfStr, true);
	}

	@Override
	protected void createLineBreakpointInteractive(IWorkbenchPart part, String sourceHandle, IResource resource,
			int lineNumber) throws CoreException {
		ICDynamicPrintf dprintf = (ICDynamicPrintf) CDIDebugModel.createBlankLineDynamicPrintf();
		Map<String, Object> attributes = new HashMap<>();
		CDIDebugModel.setLineBreakpointAttributes(attributes, sourceHandle, getBreakpointType(), lineNumber, true, 0,
				""); //$NON-NLS-1$

		// Although the user will be given the opportunity to provide the printf string
		// in the properties dialog, we pre-fill it with the default string to be nice.
		attributes.put(ICDynamicPrintf.PRINTF_STRING,
				NLS.bind(Messages.Default_LineDynamicPrintf_String, escapeBackslashes(sourceHandle), lineNumber));

		openBreakpointPropertiesDialog(dprintf, part, resource, attributes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.AbstractDisassemblyBreakpointsTarget#createAddressBreakpoint(org.eclipse.core.resources.IResource, org.eclipse.cdt.core.IAddress)
	 */
	@Override
	protected void createAddressBreakpoint(IResource resource, IAddress address) throws CoreException {
		// We provide a default printf string to make the dynamic printf useful automatically
		String format = getAddressFormat(address);
		String printfStr = NLS.bind(Messages.Default_AddressDynamicPrintf_String, address, format);

		CDIDebugModel.createAddressDynamicPrintf(null, null, resource, getBreakpointType(), -1, address, true, 0, "", //$NON-NLS-1$
				printfStr, true);
	}

	/**
	 * @param address
	 * @return
	 */
	private String getAddressFormat(IAddress address) {
		String format = "0x%x"; //$NON-NLS-1$
		if (address.getValue().bitLength() > 32)
			format = "0x%llx"; //$NON-NLS-1$
		return format;
	}

	@Override
	protected void createAddressBreakpointInteractive(IWorkbenchPart part, IResource resource, IAddress address)
			throws CoreException {
		ICDynamicPrintf dprintf = (ICDynamicPrintf) CDIDebugModel.createBlankAddressDynamicPrintf();
		Map<String, Object> attributes = new HashMap<>();
		CDIDebugModel.setAddressBreakpointAttributes(attributes, null, null, getBreakpointType(), -1, address, true, 0,
				""); //$NON-NLS-1$

		String format = getAddressFormat(address);
		String printfStr = NLS.bind(Messages.Default_AddressDynamicPrintf_String, address, format);
		attributes.put(ICDynamicPrintf.PRINTF_STRING, printfStr);

		openBreakpointPropertiesDialog(dprintf, part, resource, attributes);
	}

	protected int getBreakpointType() {
		return ICBreakpointType.REGULAR;
	}

	/**
	 * Escape embedded backslashes for inclusion in C string.
	 */
	private static String escapeBackslashes(String str) {
		return str.replaceAll(Pattern.quote("\\"), "\\\\\\\\"); //$NON-NLS-1$//$NON-NLS-2$
	}

}
