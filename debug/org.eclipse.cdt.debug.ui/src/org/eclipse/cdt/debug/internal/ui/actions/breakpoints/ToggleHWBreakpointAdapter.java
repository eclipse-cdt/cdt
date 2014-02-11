/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Tien Hock Loh (thloh@altera.com) - H/W breakpoint feature - bugzilla 332993
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions.breakpoints;

import org.eclipse.cdt.debug.core.model.ICBreakpointType;

/**
 * Toggles a line hardware breakpoint in a C/C++ editor.
 */

public class ToggleHWBreakpointAdapter extends ToggleBreakpointAdapter {
	@Override
	protected int getBreakpointType() {
		return ICBreakpointType.HARDWARE;
	}
}

