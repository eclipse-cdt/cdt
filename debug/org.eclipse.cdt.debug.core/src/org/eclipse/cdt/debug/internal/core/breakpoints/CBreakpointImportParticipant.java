/*******************************************************************************
 * Copyright (c) 2015 Freescale Semiconductors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Dan Ungureanu (Freescale Semiconductors) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.breakpoints;

import java.util.Map;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICEventBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IBreakpointImportParticipant;

/**
 * Initial implementation covering matching breakpoints at import for all platform C/C++ breakpoints
 */
public class CBreakpointImportParticipant implements IBreakpointImportParticipant {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.model.IBreakpointImportParticipant#matches(java
	 * .util.Map, org.eclipse.debug.core.model.IBreakpoint)
	 */
	public boolean matches(Map<String, Object> attributes, IBreakpoint breakpoint)
			throws CoreException {
		if (attributes == null || breakpoint == null) {
			return false;
		}
		String type = (String) attributes.get("type"); //$NON-NLS-1$
		if (type == null) {
			return false;
		}
		if (!breakpoint.getMarker().getType().equals(type)) {
			return false;
		}
		if (breakpoint instanceof AbstractLineBreakpoint) {
			return matchesLineBreakpoint(attributes,
					(AbstractLineBreakpoint) breakpoint);
		}
		if (breakpoint instanceof AbstractTracepoint) {
			return matchesTracepoint(attributes,
					(AbstractTracepoint) breakpoint);
		}
		if (breakpoint instanceof CEventBreakpoint) {
			return matchesEventBreakpoint(attributes, (CEventBreakpoint) breakpoint);
		}
		if (breakpoint instanceof CWatchpoint) {
			return matchesWatchpoint(attributes, (CWatchpoint) breakpoint);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IBreakpointImportParticipant#verify(org.eclipse.debug.core.model.IBreakpoint)
	 */
	@Override
	public void verify(IBreakpoint breakpoint) throws CoreException {}


	/**
	 * Compares two attributes in a <code>null</code> safe way
	 * 
	 * @param attr1
	 *            the first attribute
	 * @param attr2
	 *            the second attribute
	 * @return true if the attributes are equal, false otherwise. If both
	 *         attributes are <code>null</code> they are considered to be equal
	 */
	private boolean attributesEqual(Object attr1, Object attr2) {
		if (attr1 == null) {
			return attr2 == null;
		}
		return attr1.equals(attr2);
	}

	/**
	 * Returns if the given map of attributes matches the given breakpoint
	 * Works for any breakpoint class that extends AbstractLineBreakpoint
	 * 
	 * @param attributes
	 * @param breakpoint
	 * @return true if the attributes match the breakpoint's attributes, false
	 *         otherwise
	 * @throws CoreException
	 */
	private boolean matchesLineBreakpoint(Map<String, Object> attributes, AbstractLineBreakpoint breakpoint)
			throws CoreException {
		Integer line = (Integer) attributes.get(IMarker.LINE_NUMBER);
		int lineNumber = (line == null ? -1 : line.intValue());
		return breakpoint.getLineNumber() == lineNumber
				&& attributesEqual(breakpoint.getType(), attributes.get(CBreakpoint.TYPE))
				&& attributesEqual(breakpoint.getSourceHandle(), attributes.get(ICBreakpoint.SOURCE_HANDLE))
				&& attributesEqual(breakpoint.getMarker().getAttribute(ICLineBreakpoint.ADDRESS), attributes.get(ICLineBreakpoint.ADDRESS))
				&& attributesEqual(breakpoint.getMarker().getAttribute(ICLineBreakpoint.FUNCTION), attributes.get(ICLineBreakpoint.FUNCTION));
	}

	/**
	 * Returns if the given map of attributes matches the given tracepoint
	 * Works for any breakpoint class that extends AbstractTracepoint
	 * 
	 * @param attributes
	 * @param tracepoint
	 * @return true if the attributes match the tracepoint's attributes, false
	 *         otherwise
	 * @throws CoreException
	 */
	private boolean matchesTracepoint(Map<String, Object> attributes, AbstractTracepoint tracepoint)
			throws CoreException {
		Integer line = (Integer) attributes.get(IMarker.LINE_NUMBER);
		int lineNumber = (line == null ? -1 : line.intValue());
		return tracepoint.getLineNumber() == lineNumber
				&& attributesEqual(tracepoint.getType(), attributes.get(CBreakpoint.TYPE))
				&& attributesEqual(tracepoint.getSourceHandle(), attributes.get(ICBreakpoint.SOURCE_HANDLE))
				&& attributesEqual(tracepoint.getMarker().getAttribute(ICLineBreakpoint.ADDRESS), attributes.get(ICLineBreakpoint.ADDRESS))
				&& attributesEqual(tracepoint.getMarker().getAttribute(ICLineBreakpoint.FUNCTION), attributes.get(ICLineBreakpoint.FUNCTION));
	}

	/**
	 * Returns if the given map of attributes matches the given event breakpoint
	 * 
	 * @param attributes
	 * @param breakpoint
	 * @return true if the attributes match the event breakpoint's attributes, false
	 *         otherwise
	 * @throws CoreException
	 */
	private boolean matchesEventBreakpoint(Map<String, Object> attributes, CEventBreakpoint breakpoint)
			throws CoreException {
		return breakpoint.getEventArgument().equals(
				attributes.get(ICEventBreakpoint.EVENT_ARG))
				&& attributesEqual(breakpoint.getEventType(),
						attributes.get(ICEventBreakpoint.EVENT_TYPE_ID));
	}

	/**
	 * Returns if the given map of attributes matches the given watchpoint
	 * 
	 * @param attributes
	 * @param breakpoint
	 * @return true if the attributes match the watchpoint's attributes, false
	 *         otherwise
	 * @throws CoreException
	 */
	private boolean matchesWatchpoint(Map<String, Object> attributes, CWatchpoint watchpoint)
			throws CoreException {
		return watchpoint.getExpression().equals(
				attributes.get(CWatchpoint.EXPRESSION))
				&& attributesEqual(watchpoint.getType(),
						attributes.get(CBreakpoint.TYPE));
	}

}
