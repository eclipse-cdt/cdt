package org.eclipse.cdt.launch.internal.ui;

import java.util.ArrayList;

import org.eclipse.cdt.launch.ui.CArgumentsTab;
import org.eclipse.cdt.launch.ui.CDebuggerTab;
import org.eclipse.cdt.launch.ui.CEnvironmentTab;
import org.eclipse.cdt.launch.ui.CMainTab;
import org.eclipse.debug.core.ILaunchManager;
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
		ArrayList tabs = new ArrayList(5);

		tabs.add(new CMainTab());
		tabs.add(new CArgumentsTab());
		tabs.add(new CEnvironmentTab());
		if ( mode.equalsIgnoreCase(ILaunchManager.DEBUG_MODE) ) {
			tabs.add(new CDebuggerTab() );
		}
		tabs.add(new CommonTab());

		setTabs((ILaunchConfigurationTab[])tabs.toArray(new ILaunchConfigurationTab[tabs.size()]));
	}
	
}
