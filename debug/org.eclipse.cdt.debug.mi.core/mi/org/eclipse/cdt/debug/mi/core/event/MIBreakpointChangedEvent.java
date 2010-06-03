/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.event;

import org.eclipse.cdt.debug.mi.core.MISession;

public class MIBreakpointChangedEvent extends MIChangedEvent {

	/**
	 * We need these flags to notify the upper layer what kind of a breakpoint
	 * has been set from the console.
	 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=135250 
	 */
	public static final int HINT_NONE = 0;
	public static final int HINT_NEW_LINE_BREAKPOINT = 1;
	public static final int HINT_NEW_FUNCTION_BREAKPOINT = 2;
	public static final int HINT_NEW_ADDRESS_BREAKPOINT = 3;
	public static final int HINT_NEW_EVENTBREAKPOINT = 4;

	int no = 0;
	int hint = HINT_NONE;

	public MIBreakpointChangedEvent(MISession source, int number) {
		this(source, 0, number, 0);
	}

	public MIBreakpointChangedEvent(MISession source, int number, int hint) {
		this(source, 0, number, hint);
	}

	public MIBreakpointChangedEvent(MISession source, int id, int number, int hint) {
		super(source, id);
		this.no = number;
		this.hint = hint;
	}

	public int getNumber() {
		return no;
	}

	public int getHint() {
		return hint;
	}
}
