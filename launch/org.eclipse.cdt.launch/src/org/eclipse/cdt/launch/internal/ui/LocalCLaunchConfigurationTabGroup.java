package org.eclipse.cdt.launch.internal.ui;

import org.eclipse.cdt.launch.ui.CArgumentsTab;
import org.eclipse.cdt.launch.ui.CDebuggerTab;
import org.eclipse.cdt.launch.ui.CEnvironmentTab;
import org.eclipse.cdt.launch.ui.CMainTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

/**
 * Insert the type's description here.
 * @see AbstractLaunchConfigurationTabGroup
 */
public class LocalCLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {
	
	/**
	 * Insert the method's description here.
	 * @see AbstractLaunchConfigurationTabGroup#createTabs
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode)  {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
			new CMainTab(),
			new CArgumentsTab(),
			new CEnvironmentTab(),
			new CDebuggerTab(),
			new CommonTab() 
		};
		setTabs(tabs);
	}
	
}
