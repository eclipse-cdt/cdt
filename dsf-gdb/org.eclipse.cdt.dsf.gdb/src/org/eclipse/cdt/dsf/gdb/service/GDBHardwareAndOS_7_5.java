/*******************************************************************************
 * Copyright (c) 2011, 2014 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Prus (Mentor Graphics) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.service;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfoOsInfo;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 4.2
 */
public class GDBHardwareAndOS_7_5 extends GDBHardwareAndOS {

	public GDBHardwareAndOS_7_5(DsfSession session) {
		super(session);
	}

	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new RequestMonitor(ImmediateExecutor.getInstance(), requestMonitor) {
			@Override
			protected void handleSuccess() {
				register(new String[] { IGDBHardwareAndOS2.class.getName() }, new Hashtable<String, String>());

				requestMonitor.done();
			}
		});
	}

	@Override
	public void getResourceClasses(final IDMContext dmc, final DataRequestMonitor<IResourceClass[]> rm) {

		IGDBControl control = getServicesTracker().getService(IGDBControl.class);
		if (control == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Service not available", null)); //$NON-NLS-1$
			return;
		}

		CommandFactory factory = control.getCommandFactory();
		control.queueCommand(factory.createMIInfoOS(dmc), new DataRequestMonitor<MIInfoOsInfo>(getExecutor(), rm) {
			@Override
			@ConfinedToDsfExecutor("fExecutor")
			protected void handleSuccess() {
				rm.setData(getData().getResourceClasses());
				rm.done();
			}
		});
	}

	@Override
	public void getResourcesInformation(final IDMContext dmc, final String resourceClass,
			final DataRequestMonitor<IResourcesInformation> rm) {

		IGDBControl control = getServicesTracker().getService(IGDBControl.class);
		if (control == null) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Service not available", null)); //$NON-NLS-1$
			return;
		}

		CommandFactory factory = control.getCommandFactory();
		control.queueCommand(factory.createMIInfoOS(dmc, resourceClass),
				new DataRequestMonitor<MIInfoOsInfo>(getExecutor(), rm) {

					@Override
					protected void handleSuccess() {
						rm.setData(getData().getResourcesInformation());
						rm.done();
					}
				});
	}
}
