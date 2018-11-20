/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.command;

import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.gdb.launching.FinalLaunchSequence_7_12;
import org.eclipse.cdt.dsf.mi.service.command.AbstractMIControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.IEventProcessor;
import org.eclipse.cdt.dsf.mi.service.command.MIRunControlEventProcessor_7_12;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Need a new FinalLaunchSequence for GDB 7.12
 * @since 5.2
 */
public class GDBControl_7_12 extends GDBControl_7_7 {
	public GDBControl_7_12(DsfSession session, ILaunchConfiguration config, CommandFactory factory) {
		super(session, config, factory);
	}

	@Override
	protected Sequence getCompleteInitializationSequence(Map<String, Object> attributes,
			RequestMonitorWithProgress rm) {
		return new FinalLaunchSequence_7_12(getSession(), attributes, rm);
	}

	@Override
	protected IEventProcessor createMIRunControlEventProcessor(AbstractMIControl connection,
			ICommandControlDMContext controlDmc) {
		return new MIRunControlEventProcessor_7_12(connection, controlDmc);
	}

}
