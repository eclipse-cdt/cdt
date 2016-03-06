/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.command;

import java.io.IOException;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackendWithConsole;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * @since 5.0
 */
public class GDBControl_7_12 extends GDBControl_7_7 {
    public GDBControl_7_12(DsfSession session, ILaunchConfiguration config, CommandFactory factory) {
    	super(session, config, factory);
    }
    
    @Override
    protected Process createBackendCLIProcess(ICommandControlService commandControl,
    										  IMIBackend backend) throws IOException {

    	if (backend instanceof IGDBBackendWithConsole) {
    		return ((IGDBBackendWithConsole)backend).getProcess();
    	}
    	return null;
    }
}
