/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Tien Hock Loh (thloh@altera.com) - H/W breakpoint feature - bugzilla 332993
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions.breakpoints;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.debug.internal.ui.actions.ActionMessages;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Copied and modified from ToggleCBreakpointsTargetFactory
 */
public class ToggleCHWBreakpointsTargetFactory implements IToggleBreakpointsTargetFactory {

	public static String TOGGLE_C_HWBREAKPOINT_TARGET_ID = CDebugUIPlugin.getUniqueIdentifier() + ".toggleCHWBreakpointTarget"; //$NON-NLS-1$

	private static Set<String> TOGGLE_TARGET_IDS = new HashSet<String>(1);
	static {
		TOGGLE_TARGET_IDS.add(TOGGLE_C_HWBREAKPOINT_TARGET_ID);
	}

	private ToggleHWBreakpointAdapter fCToggleHWBreakpointTarget = new ToggleHWBreakpointAdapter();

	public IToggleBreakpointsTarget createToggleTarget(String targetID) {

		if (TOGGLE_C_HWBREAKPOINT_TARGET_ID.equals(targetID)) {
			return fCToggleHWBreakpointTarget;
		}
		return null;
	}

	public String getDefaultToggleTarget(IWorkbenchPart part, ISelection selection) {
		// we have no preference
		return null;
	}

	public String getToggleTargetDescription(String targetID) {
		return ActionMessages.getString("ToggleCBreakpointsTargetFactory.CHWBreakpointDescription"); //$NON-NLS-1$
	}

	public String getToggleTargetName(String targetID) {
		return ActionMessages.getString("ToggleCBreakpointsTargetFactory.CHWBreakpointName"); //$NON-NLS-1$
	}

	public Set<String> getToggleTargets(IWorkbenchPart part, ISelection selection) {
		return TOGGLE_TARGET_IDS;
	}

}

