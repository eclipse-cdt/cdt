/*******************************************************************************
 * Copyright (c) 2008, 2015 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *    Red Hat Inc. - modified for use in container launching
 *******************************************************************************/
package org.eclipse.cdt.internal.docker.launcher;

import java.util.ArrayList;

import org.eclipse.cdt.launch.ui.CArgumentsTab;
import org.eclipse.cdt.launch.ui.CMainTab2;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;

public class ContainerLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ArrayList<AbstractLaunchConfigurationTab> tabs = new ArrayList<>();
		tabs.add(new CMainTab2());
		tabs.add(new CArgumentsTab());

		tabs.add(new ContainerTab());
		tabs.add(new EnvironmentTab());

		if (mode.equals(ILaunchManager.DEBUG_MODE))
			tabs.add(new RemoteDebuggerTab());

		tabs.add(new SourceLookupTab());
		tabs.add(new CommonTab());

		setTabs(tabs.toArray(new AbstractLaunchConfigurationTab[tabs.size()]));
	}

}