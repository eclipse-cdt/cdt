/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.gdb.service;

import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.gdb.service.extensions.GDBControl_HEAD;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.gdb.launch.GdbExtendedFinalLaunchSequence;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Class that extends GDBControl.
 *
 * Note that by extending the GDBControl_HEAD class, we will always extend
 * the latest version of the GDBControl service.  The downside of this is
 * that we will automatically bring in the latest version of the service
 * even for the older GDB version that originally used GDBExtendedControl.
 * This is because of how GDBExtendedControl is instantiated in
 * GdbExtendedDebugServicesFactory.
 *
 * As we want to focus on the latest version of GDB, this is still the simplest
 * solution to use.
 *
 */
public class GDBExtendedControl extends GDBControl_HEAD {
	public GDBExtendedControl(DsfSession session, ILaunchConfiguration config, CommandFactory factory) {
		super(session, config, factory);
	}

	@Override
	protected Sequence getCompleteInitializationSequence(Map<String, Object> attributes,
			RequestMonitorWithProgress rm) {
		return new GdbExtendedFinalLaunchSequence(getSession(), attributes, rm);
	}
}
