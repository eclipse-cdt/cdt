package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIInfoOs;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfoOsInfo;
import org.eclipse.cdt.dsf.service.DsfSession;

public class GDBHardwareAndOS_7_5 extends GDBHardwareAndOS {
	
    public GDBHardwareAndOS_7_5(DsfSession session) {
    	super(session);
    }
    
	@Override
	public void getResourceClasses(final IDMContext dmc, final DataRequestMonitor<IResourceClass[]> rm) {
		
		getExecutor().submit(new DsfRunnable() {
			
			@Override
			public void run() {
				IGDBControl control = getServicesTracker().getService(IGDBControl.class);
				control.queueCommand(new MIInfoOs(dmc), new DataRequestMonitor<MIInfoOsInfo>(getExecutor(), rm) {
					@Override
					@ConfinedToDsfExecutor("fExecutor")
					protected void handleCompleted() {
						rm.setData(getData().getResourceClasses());
						super.handleCompleted();
					}
				});
				
			}			
		});
		
	}

	@Override
	public void getResourcesInformation(final IDMContext dmc, final String resourceClass, final DataRequestMonitor<IResourcesInformation> rm) {
		
		getExecutor().submit(new DsfRunnable() {

			@Override
			public void run() {
				IGDBControl control = getServicesTracker().getService(IGDBControl.class);
				control.queueCommand(new MIInfoOs(dmc, resourceClass), new DataRequestMonitor<MIInfoOsInfo>(getExecutor(), rm) {

					@Override
					protected void handleCompleted() {
						
						rm.setData(getData().getResourcesInformation());

						super.handleCompleted();
					}
				});
			}
		});
	}    
}
