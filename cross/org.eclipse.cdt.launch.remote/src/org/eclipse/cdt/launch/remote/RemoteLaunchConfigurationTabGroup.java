/*******************************************************************************
 * Copyright (c) 2006, 2009 PalmSource, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Ewa Matejska    (PalmSource)      - Adapted from LocalRunLaunchConfigurationTabGroup
 * Anna Dushistova (Mentor Graphics) - [314659] move remote launch/debug to DSF 
 *******************************************************************************/

package org.eclipse.cdt.launch.remote;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

/**
 * This class defines the tab group for the Remote C++ Launch Configuration. It
 * returns an empty set of tabs because all the tabs are contributed via
 * launchConfigurationTabs extension point
 */
public class RemoteLaunchConfigurationTabGroup extends
		AbstractLaunchConfigurationTabGroup {
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {};
		setTabs(tabs);
	}
}