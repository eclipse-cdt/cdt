/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.debug.gdbjtag.core.dsf.gdb.service;

import org.eclipse.cdt.dsf.debug.service.command.ICommandResult;
import org.eclipse.cdt.dsf.debug.service.command.ICommandToken;
import org.eclipse.cdt.dsf.mi.service.command.IEventProcessor;


/**
 * @since 9.1
 */
public class GDBJtagNullEventProcessor implements IEventProcessor {
	@Override
	public void eventReceived(Object output) {}
	@Override
	public void commandQueued(ICommandToken token) {}
	@Override
	public void commandSent(ICommandToken token) {}
	@Override 
	public void commandRemoved(ICommandToken token) {}
	@Override
	public void commandDone(ICommandToken token, ICommandResult result) {}
	@Override 
	public void dispose() {}
}