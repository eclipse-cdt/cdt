/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.gdbjtag.core.dsf.gdb.service;

import java.util.Map;

import org.eclipse.cdt.debug.gdbjtag.core.GDBJtagDSFFinalLaunchSequence;
import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.command.GDBControl_7_0;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;


/**
 * Jtag control service which selects the Jtag CompleteInitializationSequence.
 * Use for GDB >= 7.0
 */
public class GDBJtagControl_7_0 extends GDBControl_7_0 {

	public GDBJtagControl_7_0(DsfSession session, ILaunchConfiguration config, CommandFactory factory) {
		super(session, config, factory);
	}

	@Override
	protected Sequence getCompleteInitializationSequence(Map<String,Object> attributes, RequestMonitorWithProgress rm) {
		GdbLaunch launch = (GdbLaunch)getSession().getModelAdapter(ILaunch.class);
		IGDBBackend backend = getServicesTracker().getService(IGDBBackend.class);
		return new GDBJtagDSFFinalLaunchSequence(getExecutor(), launch, backend.getSessionType(), backend.getIsAttachSession(), rm);
	}
}