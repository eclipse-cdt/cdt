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
package org.eclipse.cdt.launch.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

/**
 * CDebugger tab to use for an application launch configuration.
 * 
 * @since 6.0
 */
public class ApplicationCDebuggerTab extends CDebuggerTab {
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
	private final static String DEFAULTS_SET = "org.eclipse.cdt.launch.ui.ApplicationCDebuggerTab.DEFAULTS_SET"; //$NON-NLS-1$
	
	public ApplicationCDebuggerTab() {
        super (false);
    }
    
    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(DEFAULTS_SET, true);
    	
    	super.setDefaults(config);
    }
    
    @Override
    public void initializeFrom(ILaunchConfiguration config) {
    	try {
    		if (config.hasAttribute(DEFAULTS_SET) == false) {
    			ILaunchConfigurationWorkingCopy wc;
    			wc = config.getWorkingCopy();
    			setDefaults(wc);
    			wc.doSave();
    		}
    	} catch (CoreException e) {
    	}
    	
		super.initializeFrom(config);
    }
}
