/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.breakpoints;

import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.ibm.icu.text.MessageFormat;

/**
 * A DynamicPrintf that prints a message when a particular address is reached.
 *
 * @since 7.5
 */
public class CAddressDynamicPrintf extends AbstractDynamicPrintf implements ICAddressBreakpoint {

	public CAddressDynamicPrintf() {
	}

	public CAddressDynamicPrintf(IResource resource, Map<String, Object> attributes, boolean add) throws CoreException {
		super(resource, attributes, add);
	}

	/**
	 * Returns the type of marker associated with this type of breakpoints
	 */
	@Override
	public String getMarkerType() {
		return C_ADDRESS_DYNAMICPRINTF_MARKER;
	}

	@Override
	protected String getMarkerMessage() throws CoreException {
		return MessageFormat.format(BreakpointMessages.getString("CAddressDynamicPrintf.0"), //$NON-NLS-1$
				(Object[]) new String[] { CDebugUtils.getBreakpointText(this, false) });
	}
}
