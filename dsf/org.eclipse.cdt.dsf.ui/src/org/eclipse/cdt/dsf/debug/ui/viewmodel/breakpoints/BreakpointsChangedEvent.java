/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems and others.
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
package org.eclipse.cdt.dsf.debug.ui.viewmodel.breakpoints;

import org.eclipse.debug.core.model.IBreakpoint;

/**
 * @since 2.1
 */
public class BreakpointsChangedEvent {
	public enum Type {
		ADDED, REMOVED, CHANGED
	}

	private final Type fType;
	private final IBreakpoint[] fBreakpoints;

	public BreakpointsChangedEvent(Type type, IBreakpoint[] breakpoints) {
		fType = type;
		fBreakpoints = breakpoints;
	}

	public Type getType() {
		return fType;
	}

	public IBreakpoint[] getBreakpoints() {
		return fBreakpoints;
	}
}
