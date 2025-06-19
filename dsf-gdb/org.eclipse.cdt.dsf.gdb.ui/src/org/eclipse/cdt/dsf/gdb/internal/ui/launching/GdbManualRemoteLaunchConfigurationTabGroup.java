/*******************************************************************************
 * Copyright (c) 2025 Intel corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.launch.ui.corebuild.CoreBuildMainTab2;
import org.eclipse.cdt.launch.ui.corebuild.CoreBuildTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;

public class GdbManualRemoteLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {

		List<ILaunchConfigurationTab> tabs = new ArrayList<>();
		tabs.add(new CoreBuildMainTab2());
		tabs.add(new CoreBuildTab());
		tabs.add(new CArgumentsTab());
		tabs.add(new EnvironmentTab());
		tabs.add(new RemoteApplicationCDebuggerTab());
		tabs.add(new SourceLookupTab());
		tabs.add(new CommonTab());

		setTabs(tabs.toArray(new ILaunchConfigurationTab[0]));
	}
}
