package org.eclipse.cdt.dsf.gdb.internal.ui.console.actions;

import org.eclipse.cdt.dsf.datamodel.DataModelInitializedEvent;
import org.eclipse.cdt.dsf.gdb.internal.service.IGDBSynchronizer;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.ConsoleMessages;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.IConsoleImagesConst;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

public class GdbConsoleSyncEnabledAction extends Action {

	DsfSession fSession;
	private GdbLaunch fLaunch;
	private IGDBSynchronizer fGdbSync;
	
	
	public GdbConsoleSyncEnabledAction(GdbLaunch launch) {
		super(ConsoleMessages.ConsoleSyncEnabledAction_name, IAction.AS_CHECK_BOX);
		fLaunch = launch;
		fSession = fLaunch.getSession();
		init();
//		setChecked(true);
		
		setToolTipText(ConsoleMessages.ConsoleSyncEnabledAction_description);
		setImageDescriptor(GdbUIPlugin.getImageDescriptor(IConsoleImagesConst.IMG_CONSOLE_SYNCHRONIZATION_ACTIVE_IMG));
		setDisabledImageDescriptor(GdbUIPlugin.getImageDescriptor(IConsoleImagesConst.IMG_CONSOLE_SYNCHRONIZATION_DISABLED_IMG));
	}
	
	
	private void init() {
		fLaunch.getDsfExecutor().execute(new Runnable() {
			@Override
			public void run() {
				DsfServicesTracker tracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), fSession.getId());
				fGdbSync = tracker.getService(IGDBSynchronizer.class);
				tracker.dispose();
				
				// the session may not be ready yet - if that's the case, register a listener that
				// will be called when it's ready. See handleEvent() below.
				if (fGdbSync == null) {
					fSession.addServiceEventListener(GdbConsoleSyncEnabledAction.this, null);
				}
				else {
					setChecked(fGdbSync.isSyncEnabled());
				}
			}
		});
	}

	@Override
	public void run() {
		if (fGdbSync != null) {
			
			setChecked(!fGdbSync.isSyncEnabled());
			fGdbSync.setSyncEnabled(isChecked());
		}
	}
	
	/** Invoked when the debug data model is ready */
	@DsfServiceEventHandler
	public void handleEvent(DataModelInitializedEvent event) {
		init();
		fSession.removeServiceEventListener(this);
	}
}
