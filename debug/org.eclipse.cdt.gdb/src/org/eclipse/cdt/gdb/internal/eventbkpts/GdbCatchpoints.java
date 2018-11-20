/*******************************************************************************
 * Copyright (c) 2010 Freescale Semiconductor and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Freescale Semiconductor - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.gdb.internal.eventbkpts;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.gdb.eventbkpts.IEventBreakpointConstants;

public class GdbCatchpoints {

	/** Map which services {@link #eventToGdbCatchpointKeyword(String)}  */
	private static final Map<String, String> sIdToKeyword = new HashMap<>();
	static {
		// these Ids are also referenced in mi.ui plugin as contribution
		// to event breakpoints selector
		sIdToKeyword.put(IEventBreakpointConstants.EVENT_TYPE_CATCH, "catch"); //$NON-NLS-1$
		sIdToKeyword.put(IEventBreakpointConstants.EVENT_TYPE_THROW, "throw"); //$NON-NLS-1$
		sIdToKeyword.put(IEventBreakpointConstants.EVENT_TYPE_EXEC, "exec"); //$NON-NLS-1$
		sIdToKeyword.put(IEventBreakpointConstants.EVENT_TYPE_FORK, "fork"); //$NON-NLS-1$
		sIdToKeyword.put(IEventBreakpointConstants.EVENT_TYPE_VFORK, "vfork"); //$NON-NLS-1$
		sIdToKeyword.put(IEventBreakpointConstants.EVENT_TYPE_SYSCALL, "syscall"); //$NON-NLS-1$
	}

	/**
	 * Get the gdb catchpoint event keyword associated with the given
	 * {@link IEventBreakpointConstants} event type ID. Answer will be, e.g.,
	 * "catch", "throw", "fork", etc.
	 *
	 * @param event
	 *            an EVENT_TYPE_XXXX constant from IEventBreakpointConstants
	 * @return the gdb keyword for [event]; null if [event] is unrecognized
	 */
	public static String eventToGdbCatchpointKeyword(String event) {
		String keyword = sIdToKeyword.get(event);
		assert keyword != null : "unexpected catchpoint event id";
		return keyword;
	}

	/**
	 * An inversion of the lookup done by
	 * {@link #eventToGdbCatchpointKeyword(String)}
	 *
	 * @param keyword
	 *            a gdb catchpoint keyword, e.g., "catch", "throw", "fork"
	 * @return the EVENT_TYPE_XXXX constant from IEventBreakpointConstants
	 *         associated with [keyword], or null if not recognized
	 */
	public static String gdbCatchpointKeywordToEvent(String keyword) {
		for (String eventId : sIdToKeyword.keySet()) {
			String thisKeyword = sIdToKeyword.get(eventId);
			if (thisKeyword.equals(keyword)) {
				return eventId;
			}
		}

		// Don't assert. Caller may be using us to determine if a token is a
		// catchpoint keyword. He may have parsed the keyword out from gdb/mi
		// output.
		return null;
	}
}
