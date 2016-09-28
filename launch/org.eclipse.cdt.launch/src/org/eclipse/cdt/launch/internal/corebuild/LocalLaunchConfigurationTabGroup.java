/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.launch.internal.corebuild;

import org.eclipse.cdt.launch.ui.corebuild.CoreBuildMainTab;
import org.eclipse.cdt.launch.ui.corebuild.CoreBuildTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class LocalLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab mainTab = new CoreBuildMainTab();
		ILaunchConfigurationTab buildTab = new CoreBuildTab();

		setTabs(new ILaunchConfigurationTab[] {
				mainTab,
				buildTab
		});
	}

}
