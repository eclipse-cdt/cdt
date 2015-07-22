/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.extensions;

import org.eclipse.cdt.dsf.debug.service.IDsfDebugServicesFactory;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.GDBBreakpointsManager_7_2;
import org.eclipse.cdt.dsf.gdb.service.GdbDebugServicesFactory;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;

/**
 * Top-level class in the version hierarchy of implementations of {@link MIBreakpointsManager}.
 * <br> 
 * Extenders should subclass this class for their special needs, which will allow
 * them to always extend the most recent version of the service.
 * For example, if GDB<Service>_7_9 is added, this GDB<Service>_HEAD class
 * will be changed to extend it instead of the previous version, therefore
 * automatically allowing extenders to be extending the new class.
 * 
 * NOTE: Older versions of GDB that were already using an extending class,
 *       will automatically start using the new service version, which may
 *       not be desirable.  Extenders should update how they extend
 *       GdbDebugServicesFactory to properly choose the version of the
 *       service that should be used for older GDBs.
 *       
 *       On the contrary, not using GDB<Service>_HEAD requires the
 *       extender to update how they extend GdbDebugServicesFactory
 *       whenever a new GDB<Service> version is added.
 *       
 *       Extenders that prefer to focus on the latest GDB version are
 *       encouraged to extend GDB<Service>_HEAD.
 * 
 * @since 4.8
 */
public class GDBBreakpointsManager_HEAD extends GDBBreakpointsManager_7_2 {
	public GDBBreakpointsManager_HEAD(DsfSession session, String debugModelId) {
		super(session, debugModelId);
		
		checkGdbVersion(session);
	}
	
	protected String getMinGDBVersionSupported() { return GdbDebugServicesFactory.GDB_7_2_VERSION; }
	
	private void checkGdbVersion(DsfSession session) {
		ILaunch launch = (ILaunch)session.getModelAdapter(ILaunch.class);
		if (launch instanceof GdbLaunch) {
			IDsfDebugServicesFactory servicesFactory = ((GdbLaunch)launch).getServiceFactory();
			if (servicesFactory instanceof GdbDebugServicesFactory) {
				String version = ((GdbDebugServicesFactory)servicesFactory).getVersion();
				if (getMinGDBVersionSupported().compareTo(version) > 0) {
					GdbPlugin.log(new Status(IStatus.WARNING, GdbPlugin.PLUGIN_ID, 
							"Running older GDB version: " + version +
							" when service " + this.getClass().getName() +
							" expects version " + getMinGDBVersionSupported() + " or higher."));
				}
			}
		}
	}
}
