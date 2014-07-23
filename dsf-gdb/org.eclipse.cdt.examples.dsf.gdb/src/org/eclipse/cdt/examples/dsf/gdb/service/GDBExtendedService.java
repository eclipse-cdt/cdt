/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.gdb.service;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.command.CommandCache;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLICommand;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.gdb.GDBExamplePlugin;
import org.eclipse.cdt.examples.dsf.gdb.service.command.GdbExtendedCommandFactory_6_8;
import org.eclipse.cdt.examples.dsf.gdb.service.command.output.MIGDBVersionInfo;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

public class GDBExtendedService extends AbstractDsfService implements IGDBExtendedFunctions {
	
	private IMICommandControl fCommandControl;
	private CommandFactory fCommandFactory;
	private CommandCache fVersionCache;
	
    public GDBExtendedService(DsfSession session) {
    	super(session);
    }

    @Override
    public void initialize(final RequestMonitor requestMonitor) {
    	super.initialize(new ImmediateRequestMonitor(requestMonitor) {
    		@Override
    		protected void handleSuccess() {
    			doInitialize(requestMonitor);
			}
		});
	}
	
	private void doInitialize(RequestMonitor requestMonitor) {
		fCommandControl = getServicesTracker().getService(IMICommandControl.class);
		fCommandFactory = fCommandControl.getCommandFactory();
		fVersionCache = new CommandCache(getSession(), fCommandControl);
		fVersionCache.setContextAvailable(fCommandControl.getContext(), true);

		register(new String[] { IGDBExtendedFunctions.class.getName() },
				 new Hashtable<String, String>());
		
		requestMonitor.done();
	}


	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		unregister();
		super.shutdown(requestMonitor);
	}
	
	@Override
	protected BundleContext getBundleContext() {
		return GDBExamplePlugin.getBundleContext();
	}

	@Override
	public void print(String str, RequestMonitor rm) {
		// Don't use a cache since each printout is independent
		fCommandControl.queueCommand(
				// We really should create a class for this new command, but this works too
				new CLICommand<MIInfo>(fCommandControl.getContext(), "echo " + str), //$NON-NLS-1$
				new ImmediateDataRequestMonitor<MIInfo>(rm));
	}

	@Override
	public void getVersion(ICommandControlDMContext ctx, final DataRequestMonitor<String> rm) {
		if (fCommandFactory instanceof GdbExtendedCommandFactory_6_8) {
			GdbExtendedCommandFactory_6_8 factory = (GdbExtendedCommandFactory_6_8)fCommandFactory;

			// Use the cache to avoid having to go to GDB more than once for a value
			// that does not change.  No need to even clear the cache since the GDB version will never change.
			fVersionCache.execute(factory.createMIGDBVersion(fCommandControl.getContext()), 
					new ImmediateDataRequestMonitor<MIGDBVersionInfo>(rm) {
				@Override
				protected void handleSuccess() {
					rm.done(getData().getVersion());
				}
			});
		} else {
			rm.done(new Status(IStatus.ERROR, GDBExamplePlugin.PLUGIN_ID,
					NOT_SUPPORTED, "Not supported", null)); //$NON-NLS-1$
		}
	}
}
