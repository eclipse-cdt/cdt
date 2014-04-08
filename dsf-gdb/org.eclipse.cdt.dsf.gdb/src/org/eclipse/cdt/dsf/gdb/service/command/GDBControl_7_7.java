/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.command;

import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.launching.FinalLaunchSequence_7_7;
import org.eclipse.cdt.dsf.mi.service.command.CLIEventProcessor_7_7;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.IEventProcessor;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Need a new FinalLaunchSequence for GDB 7.7
 * @since 4.4
 */
public class GDBControl_7_7 extends GDBControl_7_4 {
    public GDBControl_7_7(DsfSession session, ILaunchConfiguration config, CommandFactory factory) {
    	super(session, config, factory);
    }
    
	@Override
	protected IEventProcessor createCLIEventProcessor(ICommandControlService connection, ICommandControlDMContext controlDmc) {
		return new CLIEventProcessor_7_7(connection, controlDmc);
	}

    @Override
	protected Sequence getCompleteInitializationSequence(Map<String, Object> attributes, RequestMonitorWithProgress rm) {
		return new FinalLaunchSequence_7_7(getSession(), attributes, rm);
	}
}
