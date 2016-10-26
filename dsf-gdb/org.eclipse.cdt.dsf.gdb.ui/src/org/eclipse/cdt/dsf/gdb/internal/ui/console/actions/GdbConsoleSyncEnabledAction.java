/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.console.actions;


import org.eclipse.cdt.dsf.datamodel.DataModelInitializedEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlShutdownDMEvent;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.service.IGDBSynchronizer;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.ConsoleMessages;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.IConsoleImagesConst;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.preferences.ScopedPreferenceStore;


public class GdbConsoleSyncEnabledAction extends Action {
	private DsfSession fSession;
	private GdbLaunch fLaunch;
	private IGDBSynchronizer fGdbSync;
	private IPreferenceStore fPrefStore;
	private IPropertyChangeListener fPropertyChangeListener;
	
	public GdbConsoleSyncEnabledAction(GdbLaunch launch) {
		super(ConsoleMessages.ConsoleSyncEnabledAction_name, IAction.AS_CHECK_BOX);
		fLaunch = launch;
		fSession = fLaunch.getSession();
		fPrefStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, GdbUIPlugin.PLUGIN_ID);
		
		// add a listener so we are notified if one instance of this action
		// (corresponding on one debug session) modifies the state (enable/disable
		// sync). This way we can make sure that all instances reflect the same 
		// value, i.e. have the sync button in the same state.
		fPropertyChangeListener = new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(IGdbDebugPreferenceConstants.PREF_CONSOLE_SYNC_ENABLED)) {
					Object value = event.getNewValue();
					if (value instanceof Boolean) {
						setChecked((Boolean)value);
					}
				}
			}
		};
		
		fPrefStore.addPropertyChangeListener(fPropertyChangeListener);
		fPrefStore.setDefault(IGdbDebugPreferenceConstants.PREF_CONSOLE_SYNC_ENABLED, 
				IGdbDebugPreferenceConstants.CONSOLE_SYNC_ENABLED_DEFAULT);
		init();
		
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
				
				if (fGdbSync != null) {
					setChecked(fPrefStore.getBoolean(IGdbDebugPreferenceConstants.PREF_CONSOLE_SYNC_ENABLED));
				}
				else {
					// the sync service is not available yet for this session - register a listener
					// that will notify us when it's ready. See handleEvent() below.
					fSession.addServiceEventListener(GdbConsoleSyncEnabledAction.this, null);
				}
			}
		});
	}

	@Override
	public void run() {
		if (fGdbSync != null) {
			boolean checked = !fGdbSync.isSyncEnabled(); 
			setChecked(checked);
			fGdbSync.setSyncEnabled(checked);
			
			// Save the new value in the preference store so that all other 
			// instances of this action will be notified of the change and 
			// be able to update the state of their button
			fPrefStore.setValue(IGdbDebugPreferenceConstants.PREF_CONSOLE_SYNC_ENABLED, checked);
		}
	}
	
	@DsfServiceEventHandler
	public void handleEvent(DataModelInitializedEvent event) {
		init();
	}
	
	// 
	@DsfServiceEventHandler
	public void handleEvent(ICommandControlShutdownDMEvent event) {
		// unregister property listener
		fPrefStore.removePropertyChangeListener(fPropertyChangeListener);
	}
}
