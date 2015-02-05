/***************************************************************************************************
 * Copyright (c) 2008, 2010 Mirko Raner.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mirko Raner - initial implementation for Eclipse Bug 196337
 **************************************************************************************************/

package org.eclipse.tm.internal.terminal.local.launch.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;

/**
* The class {@link LocalTerminalLaunchTabGroup} defines the tabs for the launch configuration
* dialog that is used for terminal-based launches. The tab groups consists of the main tab for
* a standard program launch (lifted from the <code>org.eclipse.ui.externaltools</code> plug-in), the
* custom {@link LocalTerminalSettingsTab}, and the {@link EnvironmentTab} and {@link CommonTab},
* which can be publicly accessed from the <code>org.eclipse.debug.ui</code> plug-in.
*
* @author Mirko Raner
* @version $Revision: 1.2 $
**/
public class LocalTerminalLaunchTabGroup extends AbstractLaunchConfigurationTabGroup {

	private final static String ID = "id"; //$NON-NLS-1$
	private final static String CLASS = "class"; //$NON-NLS-1$
	private final static String PROGRAM_TAB_GROUP =
		"org.eclipse.ui.externaltools.launchConfigurationTabGroup.program"; //$NON-NLS-1$

	/**
	* Creates a new {@link LocalTerminalLaunchTabGroup}.
	**/
	public LocalTerminalLaunchTabGroup() {

		super();
	}

	/**
	* Creates the tabs contained in the local terminal launch configuration dialog for the specified
	* launch mode. The tabs control's are not yet created. This is the first method called in the
	* life-cycle of a tab group.
	*
	* @param dialog the launch configuration dialog this tab group is contained in
	* @param mode the mode the launch configuration dialog was opened in
	* @see AbstractLaunchConfigurationTabGroup#createTabs(ILaunchConfigurationDialog, String)
	**/
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {

		ILaunchConfigurationTab main = getMainTab(dialog, mode);
		ILaunchConfigurationTab terminal = new LocalTerminalSettingsTab();
		ILaunchConfigurationTab environment = new EnvironmentTab();
		ILaunchConfigurationTab common = new CommonTab();
		ILaunchConfigurationTab[] tabs = {main, terminal, environment, common};
		setTabs(tabs);
	}

	//-------------------------------------- PRIVATE SECTION -------------------------------------//

	private ILaunchConfigurationTab getMainTab(ILaunchConfigurationDialog dialog, String mode) {

		// Find the main tab for the external program launch in the registry (a direct search is
		// only possible for extensions that actually declare a unique ID, which most extensions
		// don't; the search for the "id" attribute of a configuration element has to be done
		// manually):
		//
		IConfigurationElement[] element;
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		final String TAB_GROUPS = IDebugUIConstants.EXTENSION_POINT_LAUNCH_CONFIGURATION_TAB_GROUPS;
		element = registry.getConfigurationElementsFor(IDebugUIConstants.PLUGIN_ID, TAB_GROUPS);
		int numberOfElements = element.length;
		for (int index = 0; index < numberOfElements; index++) {

			if (element[index].getAttribute(ID).equals(PROGRAM_TAB_GROUP)) {

				try {

					ILaunchConfigurationTabGroup tabGroup;
					Object executable = element[index].createExecutableExtension(CLASS);
					tabGroup = (ILaunchConfigurationTabGroup)executable;
					tabGroup.createTabs(dialog, mode);

					// It's not possible to make assumptions about the class name of the program
					// main tab (without over-stepping API boundaries), but it's usually the very
					// first tab in the group (which is an assumption that actually also over-steps
					// API boundaries, but it's the best possible solution, short of copying the
					// whole source code):
					//
					return tabGroup.getTabs()[0];
				}
				catch (CoreException exception) {

					Logger.logException(exception);
				}
			}
		}
		return null;
	}
}
