/*******************************************************************************
 * Copyright (c) 2008, 2010 QNX Software Systems and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * QNX Software Systems - catchpoints - bug 226689
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.breakpoints;

import java.util.Map;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICEventBreakpoint;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;

public class CEventBreakpoint extends CBreakpoint implements ICEventBreakpoint {

	public CEventBreakpoint() {
	}

	public CEventBreakpoint(IResource resource, Map<String, Object> attributes, boolean add) throws CoreException {
		this();
		// event breakpoint must set non null EVENT_TYPE_ID property to be valid
		if (attributes.get(EVENT_TYPE_ID) == null)
			throw new IllegalArgumentException();
		CDIDebugModel.createBreakpointMarker(this, resource, attributes, add);
	}

	@Override
	public String getMarkerType() {
	    return C_EVENT_BREAKPOINT_MARKER;
	}
	
	@Override
	protected String getMarkerMessage() throws CoreException {
		// default message, overridden by label provider, which would take care of translation
		return "Event Breakpoint: " + getEventType(); // $NON-NLS-1$ //$NON-NLS-1$
	}

	/**
	 * @see ICEventBreakpoint#getEventType()
	 */
	@Override
	public String getEventType() throws DebugException {
		return ensureMarker().getAttribute(EVENT_TYPE_ID, ""); //$NON-NLS-1$
	}

	/**
	 * @see ICEventBreakpoint#getEventArgument()
	 */
	@Override
	public String  getEventArgument() throws CoreException {
		return ensureMarker().getAttribute(EVENT_ARG, ""); //$NON-NLS-1$
	}

}
