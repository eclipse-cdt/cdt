/*******************************************************************************
 * Copyright (c) 2016 Ericsson.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.llvm.dsf.lldb.core.internal.service;

import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.gdb.service.command.GDBControl_7_4;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.llvm.dsf.lldb.core.internal.launching.LLDBFinalLaunchSequence;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Provides service for controlling sending and receiving commands. See
 * {@link ICommandControl}
 *
 * This LLDB specific implementation was initially created in order to be able
 * to control the launch sequence of LLDB.
 */
public class LLDBControl extends GDBControl_7_4 {

	/**
	 * Constructs the {@link LLDBControl} service.
	 *
	 * @param session
	 *            The debugging session
	 * @param config
	 *            the launch configuration
	 * @param factory
	 *            the command factory used to send commands
	 */
	public LLDBControl(DsfSession session, ILaunchConfiguration config, CommandFactory factory) {
		super(session, config, factory);
	}

	@Override
	protected Sequence getCompleteInitializationSequence(Map<String, Object> attributes,
			RequestMonitorWithProgress rm) {
		return new LLDBFinalLaunchSequence(getSession(), attributes, rm);
	}
}
