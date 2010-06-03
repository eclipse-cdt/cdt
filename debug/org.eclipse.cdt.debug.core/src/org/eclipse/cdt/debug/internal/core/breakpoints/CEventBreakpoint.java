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

import org.eclipse.cdt.debug.core.model.ICEventBreakpoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;

public class CEventBreakpoint extends CBreakpoint implements ICEventBreakpoint {

	private static final String C_EVENTBREAKPOINT_MARKER_TYPE = "org.eclipse.cdt.debug.core.cEventBreakpointMarker"; //$NON-NLS-1$;

	public CEventBreakpoint() {

	}

	public static String getMarkerType() {
		return C_EVENTBREAKPOINT_MARKER_TYPE;
	}

	public CEventBreakpoint(IResource resource, Map<String, Object> attributes, boolean add) throws CoreException {
		this();
		// event breakpoint must set non null EVENT_TYPE_ID property to be valid
		if (attributes.get(EVENT_TYPE_ID) == null)
			throw new IllegalArgumentException();
		setBreakpointMarker(resource, getMarkerType(), attributes, add);

	}

	private void setBreakpointMarker(final IResource resource, final String markerType,
			final Map<String, Object> attributes, final boolean add) throws DebugException {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				// create the marker
				setMarker(resource.createMarker(markerType));
				// set attributes
				ensureMarker().setAttributes(attributes);
				// set the marker message
				setAttribute(IMarker.MESSAGE, getMarkerMessage());
				// add to breakpoint manager if requested
				register(add);
			}
		};
		run(wr);
	}

	@Override
	protected String getMarkerMessage() throws CoreException {
		// default message, overridden by label provider, which would take care of translation
		return "Event Breakpoint: " + getEventType(); // $NON-NLS-1$ //$NON-NLS-1$
	}

	/**
	 * @see ICEventBreakpoint#getEventType()
	 */
	public String getEventType() throws DebugException {
		return ensureMarker().getAttribute(EVENT_TYPE_ID, ""); //$NON-NLS-1$
	}

	/**
	 * @see ICEventBreakpoint#getEventArgument()
	 */
	public String  getEventArgument() throws CoreException {
		return ensureMarker().getAttribute(EVENT_ARG, ""); //$NON-NLS-1$
	}

}
