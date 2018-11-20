/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
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
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.breakpoints;

import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.ibm.icu.text.MessageFormat;

/**
 * A breakpoint that suspends the execution when a function is entered.
 */
public class CFunctionBreakpoint extends AbstractLineBreakpoint implements ICFunctionBreakpoint {

	/**
	 * Constructor for CFunctionBreakpoint.
	 */
	public CFunctionBreakpoint() {
	}

	/**
	 * Constructor for CFunctionBreakpoint.
	 */
	public CFunctionBreakpoint(IResource resource, Map<String, Object> attributes, boolean add) throws CoreException {
		super(resource, attributes, add);
	}

	/**
	 * Returns the type of marker associated with this type of breakpoints
	 */
	@Override
	public String getMarkerType() {
		return C_FUNCTION_BREAKPOINT_MARKER;
	}

	/*(non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint#getMarkerMessage()
	 */
	@Override
	protected String getMarkerMessage() throws CoreException {
		return MessageFormat.format(BreakpointMessages.getString("CFunctionBreakpoint.0"), //$NON-NLS-1$
				(Object[]) new String[] { CDebugUtils.getBreakpointText(this, false) });
	}
}
