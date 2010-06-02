/*******************************************************************************
 * Copyright (c) 2006, 2009 PalmSource, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Ewa Matejska (PalmSource)
 *******************************************************************************/

package org.eclipse.cdt.launch.remote;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.launch.ui.CDebuggerTab;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

public class RemoteCDebuggerTab extends CDebuggerTab {
	
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
			"org.eclipse.rse.internal.remotecdt.launchgroup"); //$NON-NLS-1$
	}

	static final private String REMOTE_GDB_DEBUGGER_NAME = "remote gdb/mi"; //$NON-NLS-1$
	
	public RemoteCDebuggerTab(boolean attachMode) {
		super(attachMode);
	}
	
	protected void loadDebuggerComboBox(ILaunchConfiguration config, String selection) {
		ICDebugConfiguration[] debugConfigs = CDebugCorePlugin.getDefault().getDebugConfigurations();
		String defaultSelection = selection;
		List list = new ArrayList();
		for(int i = 0; i < debugConfigs.length; i++) {
			ICDebugConfiguration configuration = debugConfigs[i];
			if(configuration.getName().equals(REMOTE_GDB_DEBUGGER_NAME))  {
				list.add(configuration);
				// Select as default selection
				defaultSelection = configuration.getID();
				break;
			}
		}
		setInitializeDefault(defaultSelection.equals("") ? true : false); //$NON-NLS-1$
		loadDebuggerCombo((ICDebugConfiguration[])list.toArray(
				new ICDebugConfiguration[list.size()]), defaultSelection);
	}
	
}
