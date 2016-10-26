/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.console.actions;


import org.eclipse.cdt.dsf.datamodel.DataModelInitializedEvent;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.service.GDBSynchronizer;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.ConsoleMessages;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.IConsoleImagesConst;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * Class that implements the debugger console synchronize action toggle button. Implemented as a 
 * singleton since this way it's easier to keep the sync button in the same state for all debug 
 * sessions. 
 */
public class GdbConsoleSyncEnabledAction extends Action {
	private static GdbConsoleSyncEnabledAction fInstance;
	private IPreferenceStore fPrefStore;
	
	protected GdbConsoleSyncEnabledAction() {
		super(ConsoleMessages.ConsoleSyncEnabledAction_name, IAction.AS_CHECK_BOX);
		fPrefStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, GdbUIPlugin.PLUGIN_ID);

		fPrefStore.setDefault(IGdbDebugPreferenceConstants.PREF_CONSOLE_SYNC_ENABLED, 
				IGdbDebugPreferenceConstants.CONSOLE_SYNC_ENABLED_DEFAULT);
		init();
		
		setToolTipText(ConsoleMessages.ConsoleSyncEnabledAction_description);
		setImageDescriptor(GdbUIPlugin.getImageDescriptor(IConsoleImagesConst.IMG_CONSOLE_SYNCHRONIZATION_ACTIVE_IMG));
		setDisabledImageDescriptor(GdbUIPlugin.getImageDescriptor(IConsoleImagesConst.IMG_CONSOLE_SYNCHRONIZATION_DISABLED_IMG));
	}
	
	public static GdbConsoleSyncEnabledAction getInstance() {
		if (fInstance == null) {
			fInstance = new GdbConsoleSyncEnabledAction();
		}
		return fInstance;
	}
	
	private void init() {
		boolean enabled = fPrefStore.getBoolean(IGdbDebugPreferenceConstants.PREF_CONSOLE_SYNC_ENABLED); 
		setChecked(enabled);
		GDBSynchronizer.setSynchronizationEnabled(enabled);
	}

	@Override
	public void run() {
		boolean checked = !GDBSynchronizer.isSynchronizationEnabled(); 
		setChecked(checked);
		GDBSynchronizer.setSynchronizationEnabled(checked);

		// Save the new value in the preference store so it will be remembered for future 
		// sessions
		fPrefStore.setValue(IGdbDebugPreferenceConstants.PREF_CONSOLE_SYNC_ENABLED, checked);
	}
	
	@DsfServiceEventHandler
	public void handleEvent(DataModelInitializedEvent event) {
		init();
	}

}
