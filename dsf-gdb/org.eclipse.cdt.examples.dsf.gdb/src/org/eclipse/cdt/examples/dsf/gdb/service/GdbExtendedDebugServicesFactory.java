/*******************************************************************************
 * Copyright (c) 2014, 2016 Ericsson and others.
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

import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.gdb.service.GdbDebugServicesFactory;
import org.eclipse.cdt.dsf.gdb.service.command.GDBControl;
import org.eclipse.cdt.dsf.gdb.service.command.GDBControl_7_0;
import org.eclipse.cdt.dsf.gdb.service.command.GDBControl_7_2;
import org.eclipse.cdt.dsf.gdb.service.command.GDBControl_7_4;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.gdb.service.command.GdbExtendedCommandFactory_6_8;
import org.eclipse.debug.core.ILaunchConfiguration;

public class GdbExtendedDebugServicesFactory extends GdbDebugServicesFactory {

	public GdbExtendedDebugServicesFactory(String version, ILaunchConfiguration config) {
		super(version, config);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> V createService(Class<V> clazz, DsfSession session, Object... optionalArguments) {
		if (IGDBExtendedFunctions.class.isAssignableFrom(clazz)) {
			return (V) createExtendedService(session);
		}
		return super.createService(clazz, session, optionalArguments);
	}

	@Override
	protected ICommandControl createCommandControl(DsfSession session, ILaunchConfiguration config) {
		if (compareVersionWith(GDB_7_7_VERSION) >= 0) {
			return new GDBExtendedControl(session, config, new GdbExtendedCommandFactory_6_8());
		}
		if (compareVersionWith(GDB_7_4_VERSION) >= 0) {
			return new GDBControl_7_4(session, config, new GdbExtendedCommandFactory_6_8());
		}
		if (compareVersionWith(GDB_7_2_VERSION) >= 0) {
			return new GDBControl_7_2(session, config, new GdbExtendedCommandFactory_6_8());
		}
		if (compareVersionWith(GDB_7_0_VERSION) >= 0) {
			return new GDBControl_7_0(session, config, new GdbExtendedCommandFactory_6_8());
		}
		if (compareVersionWith(GDB_6_8_VERSION) >= 0) {
			return new GDBControl(session, config, new GdbExtendedCommandFactory_6_8());
		}
		return new GDBControl(session, config, new CommandFactory());
	}

	protected IGDBExtendedFunctions createExtendedService(DsfSession session) {
		return new GDBExtendedService(session);
	}

}
