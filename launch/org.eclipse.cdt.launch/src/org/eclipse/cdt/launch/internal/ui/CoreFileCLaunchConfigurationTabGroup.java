package org.eclipse.cdt.launch.internal.ui;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.ui.CMainTab;
import org.eclipse.cdt.launch.ui.CSourceLookupTab;
import org.eclipse.cdt.launch.ui.CorefileDebuggerTab;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

/**
 * Insert the type's description here.
 * @see AbstractLaunchConfigurationTabGroup
 */
public class CoreFileCLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

	/**
	 * Insert the method's description here.
	 * @see AbstractLaunchConfigurationTabGroup#createTabs
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode)  {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
			new CMainTab(),
			new CorefileDebuggerTab(),
			new CSourceLookupTab(),
			new CommonTab()
		};
		setTabs(tabs);
	}
	
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// This configuration should work for all platforms
		configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PLATFORM, "*");
		super.setDefaults(configuration);
	}

}
