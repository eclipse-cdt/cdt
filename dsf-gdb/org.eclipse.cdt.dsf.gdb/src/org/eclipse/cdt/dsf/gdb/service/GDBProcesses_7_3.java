/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.command.events.MIThreadGroupExitedEvent;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * Version for GDB 7.3
 * 
 * @since 4.7
 */
public class GDBProcesses_7_3 extends GDBProcesses_7_2_1 {
    
	public GDBProcesses_7_3(DsfSession session) {
		super(session);
	}

	@Override
	protected Sequence getStartOrRestartProcessSequence(DsfExecutor executor, IContainerDMContext containerDmc, 
			Map<String, Object> attributes, boolean restart, 
			DataRequestMonitor<IContainerDMContext> rm) {
		return new StartOrRestartProcessSequence_7_3(executor, containerDmc, attributes, restart, rm);
	}
	
	@Override
    @DsfServiceEventHandler
	public void eventDispatched(MIThreadGroupExitedEvent e) {
		super.eventDispatched(e);

		// Cache the exit code if there is one
		String groupId = e.getGroupId();
		String exitCode = e.getExitCode();
		if (groupId != null && exitCode != null) {
			ExitedProcInfo info = getExitedProcesses().get(groupId);
			if (info != null) {
				try {
					// Must use 'decode' since GDB returns an octal value
					info.setExitCode(Integer.decode(exitCode));
				} catch (NumberFormatException exception) {
				}    					
			}
		}
	}
}

