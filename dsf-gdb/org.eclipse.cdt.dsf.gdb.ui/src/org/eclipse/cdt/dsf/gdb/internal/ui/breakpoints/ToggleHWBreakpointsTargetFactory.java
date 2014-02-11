/*******************************************************************************
 * Copyright (c) 2007, 2010,2014 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.breakpoints;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.dsf.debug.internal.ui.DisassemblyToggleBreakpointsTarget;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblyPart;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Toggle HW breakpoints target factory for disassembly parts.
 * We use a separate factory so that we can control it through an action set.
 */
@SuppressWarnings("restriction")
public class ToggleHWBreakpointsTargetFactory implements IToggleBreakpointsTargetFactory {

	/**
	 * Toggle breakpoint target-id for normal C HW breakpoints.
	 * Note: The id must be the same as in <code>ToggleCHWBreakpointsTargetFactory</code>
	 *       which is used for the editor.  We need the id to be the same so that when
	 *       the user goes from editor to DSF disassembly view, the choice of breakpoint
	 *       targets looks the same and is remembered.
	 *       To use the same id though, we must be careful not to have the two factories
	 *       return the same id for the same part, or else it may confuse things.
	 *       This is why this factory only returns this id for the DSF disassembly part,
	 *       leaving <code>ToggleCHWBreakpointsTargetFactory</code> to return the same id
	 *       for the editor.
	 */

	public static final String TOGGLE_C_HWBREAKPOINT_TARGET_ID = CDebugUIPlugin.PLUGIN_ID + ".toggleCHWBreakpointTarget"; //$NON-NLS-1$

	private static final Set<String> TOGGLE_TARGET_IDS = new HashSet<String>(1);
	static {
		TOGGLE_TARGET_IDS.add(TOGGLE_C_HWBREAKPOINT_TARGET_ID);
	}

	private static class DisassemblyToggleHBreakpointsTarget extends DisassemblyToggleBreakpointsTarget {

	/* (non-Javadoc)
		 * @see org.eclipse.cdt.dsf.debug.internal.ui.DisassemblyToggleBreakpointsTarget#getBreakpointType()
		 */
		@Override
		protected int getBreakpointType() {
			return ICBreakpointType.HARDWARE;
		}
	}

	private static final IToggleBreakpointsTarget fgDisasemblyHWBreakpointTarget = new DisassemblyToggleHBreakpointsTarget();

	@Override
	public IToggleBreakpointsTarget createToggleTarget(String targetID) {

		if (TOGGLE_C_HWBREAKPOINT_TARGET_ID.equals(targetID)) {
			return fgDisasemblyHWBreakpointTarget;
		}
		return null;
	}

	@Override
	public String getDefaultToggleTarget(IWorkbenchPart part, ISelection selection) {
		// we have no preference
		return null;
	}

	@Override
	public String getToggleTargetDescription(String targetID) {
			return Messages.ToggleHWBreakpoints_Description;
	}

	@Override
	public String getToggleTargetName(String targetID) {
		return Messages.ToggleHWBreakpoints_Name;
	}

	@Override
	public Set<String> getToggleTargets(IWorkbenchPart part, ISelection selection) {
		if (part instanceof IDisassemblyPart) {
			return TOGGLE_TARGET_IDS;
		}
		return Collections.emptySet();
	}
 
}

