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
 *     Marc Khouzam (Ericsson) - initial API and implementation
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
 * Toggle Dynamic Printf target factory for the CEditor.
 *
 * @since 7.5
 */
public class ToggleCDynamicPrintfTargetFactory implements IToggleBreakpointsTargetFactory {

	public static String TOGGLE_C_DYNAMICPRINTF_TARGET_ID = CDebugUIPlugin.getUniqueIdentifier()
			+ ".toggleCDynamicPrintfTarget"; //$NON-NLS-1$

	private static Set<String> TOGGLE_TARGET_IDS = new HashSet<>(1);
	static {
		TOGGLE_TARGET_IDS.add(TOGGLE_C_DYNAMICPRINTF_TARGET_ID);
	}

	private ToggleDynamicPrintfAdapter fCToggleDynamicPrintfTarget = new ToggleDynamicPrintfAdapter();

	@Override
	public IToggleBreakpointsTarget createToggleTarget(String targetID) {
		if (TOGGLE_C_DYNAMICPRINTF_TARGET_ID.equals(targetID)) {
			return fCToggleDynamicPrintfTarget;
		}
		return null;
	}

	@Override
	public String getDefaultToggleTarget(IWorkbenchPart part, ISelection selection) {
		return null;
	}

	@Override
	public String getToggleTargetDescription(String targetID) {
		if (TOGGLE_C_DYNAMICPRINTF_TARGET_ID.equals(targetID)) {
			return ActionMessages.getString("ToggleCBreakpointsTargetFactory.CDynamicPrintfDescription"); //$NON-NLS-1$
		}
		return null;
	}

	@Override
	public String getToggleTargetName(String targetID) {
		if (TOGGLE_C_DYNAMICPRINTF_TARGET_ID.equals(targetID)) {
			return ActionMessages.getString("ToggleCBreakpointsTargetFactory.CDynamicPrintfName"); //$NON-NLS-1$
		}
		return null;
	}

	@Override
	public Set<String> getToggleTargets(IWorkbenchPart part, ISelection selection) {
		return TOGGLE_TARGET_IDS;
	}
}
