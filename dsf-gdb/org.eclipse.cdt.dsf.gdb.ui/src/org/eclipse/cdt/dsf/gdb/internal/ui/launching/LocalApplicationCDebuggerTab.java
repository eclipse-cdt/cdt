/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

/**
 * Debugger tab to use for a local application launch configuration.
 * 
 * @since 2.0
 */
public class LocalApplicationCDebuggerTab extends CDebuggerTab {

	/*
	 * When the launch configuration is created for Run mode,
	 * this Debugger tab is not created because it is not used
	 * for Run mode but only for Debug mode.
	 * When we then open the same configuration in Debug mode, the launch
	 * configuration already exists and initializeFrom() is called
	 * instead of setDefaults().
	 * We therefore call setDefaults() ourselves and update the configuration.
	 * If we don't then the user will be required to press Apply to get the
	 * default settings saved.
	 * Bug 281970
	 */
	private boolean fSetDefaultCalled;

    public LocalApplicationCDebuggerTab() {
        super(SessionType.LOCAL, false);
    }
    
    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    	fSetDefaultCalled = true;
    	
    	super.setDefaults(config);
    }
    
    @Override
    public void initializeFrom(ILaunchConfiguration config) {
		if (fSetDefaultCalled == false) {
			try {
				ILaunchConfigurationWorkingCopy wc;
				wc = config.getWorkingCopy();
				setDefaults(wc);
				wc.doSave();
			} catch (CoreException e) {
			}
		}

		super.initializeFrom(config);
    }
}
