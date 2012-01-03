/*******************************************************************************
 * Copyright (c) 2009 Ericsson, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.breakpoints;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblyPart;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Toggle tracepoints target factory for disassembly parts.
 * We use a separate factory so that we can control it through an action set.
 *
 * @since 2.1
 */
public class ToggleTracepointsTargetFactory implements IToggleBreakpointsTargetFactory {

	/**
	 * Toggle tracepoint target-id for normal C tracepointspoints.
	 * Note: The id must be the same as in <code>ToggleCBreakpointsTargetFactory</code>
	 *       which is used for the editor.  We need the id to be the same so that when
	 *       the user goes from editor to DSF disassembly view, the choice of breakpoint
	 *       targets looks the same and is remembered.
	 *       To use the same id though, we must be careful not to have the two factories
	 *       return the same id for the same part, or else it may confuse things.
	 *       This is why this factory only returns this id for the DSF disassembly part,
	 *       leaving <code>ToggleCBreakpointsTargetFactory</code> to return the same id
	 *       for the editor.
	 */
	public static final String TOGGLE_C_TRACEPOINT_TARGET_ID = CDebugUIPlugin.PLUGIN_ID + ".toggleCTracepointTarget"; //$NON-NLS-1$
	
	private static final Set<String> TOGGLE_TARGET_IDS_ALL = new HashSet<String>(1);
	static {
		TOGGLE_TARGET_IDS_ALL.add(TOGGLE_C_TRACEPOINT_TARGET_ID);
	}

	private static final IToggleBreakpointsTarget fgDisassemblyToggleTracepointsTarget = new DisassemblyToggleTracepointsTarget();

	public ToggleTracepointsTargetFactory() {
	}

    @Override
	public IToggleBreakpointsTarget createToggleTarget(String targetID) {
		if (TOGGLE_C_TRACEPOINT_TARGET_ID.equals(targetID)) {
			return fgDisassemblyToggleTracepointsTarget;
		}
		return null;
	}

    @Override
	public String getDefaultToggleTarget(IWorkbenchPart part, ISelection selection) {
		return null;
	}

    @Override
	public String getToggleTargetDescription(String targetID) {
		if (TOGGLE_C_TRACEPOINT_TARGET_ID.equals(targetID)) {
			return Messages.ToggleTracepointsTargetFactory_description;
		}
		return null;
	}

    @Override
	public String getToggleTargetName(String targetID) {
		if (TOGGLE_C_TRACEPOINT_TARGET_ID.equals(targetID)) {
			return Messages.ToggleTracepointsTargetFactory_name;
		}
		return null;
	}

    @Override
	public Set<String> getToggleTargets(IWorkbenchPart part, ISelection selection) {
		if (part instanceof IDisassemblyPart) {
			return TOGGLE_TARGET_IDS_ALL;
		}
		return Collections.emptySet();
	}

}
