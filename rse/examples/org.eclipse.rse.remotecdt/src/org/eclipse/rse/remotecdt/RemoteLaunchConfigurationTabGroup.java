/*******************************************************************************
 * Copyright (c) 2006 PalmSource, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Ewa Matejska (PalmSource) - Adapted from LocalRunLaunchConfigurationTabGroup
 *******************************************************************************/


package org.eclipse.rse.remotecdt;
import org.eclipse.cdt.launch.ui.CArgumentsTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;

/**
 * This class defines the tab group for the Remote C++ Launch
 * Configuration.
 */
public class RemoteLaunchConfigurationTabGroup extends
		AbstractLaunchConfigurationTabGroup {
	 public void createTabs(ILaunchConfigurationDialog dialog, String mode)  {
         ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
                 new RemoteCMainTab(true),
                 new CArgumentsTab(),
                 new SourceLookupTab(),
                 new CommonTab()
         };
         setTabs(tabs);
	 }
}