/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.command;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.IEventProcessor;
import org.eclipse.cdt.dsf.mi.service.command.MIInferiorEventProcessor_7_3;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Use different MIInferiorEventProcessor so the exit code
 * of the inferior is handled properly
 * @since 5.1
 */
public class GDBControl_7_3 extends GDBControl_7_2 {
    public GDBControl_7_3(DsfSession session, ILaunchConfiguration config, CommandFactory factory) {
    	super(session, config, factory);
    }
    
    @Override
    protected IEventProcessor createInferiorEventProcessor(ICommandControlService connection) {
		return new MIInferiorEventProcessor_7_3(connection);
    }
}
