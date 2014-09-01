package org.eclipse.cdt.launchbar.ui.internal.targetsView;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptor;
import org.eclipse.cdt.launchbar.ui.internal.Activator;
import org.eclipse.cdt.launchbar.ui.internal.LaunchBarUIManager;
import org.eclipse.ui.navigator.CommonNavigator;

public class LaunchTargetsNavigator extends CommonNavigator {

	private final LaunchBarUIManager uiManager;

	public LaunchTargetsNavigator() {
		ILaunchBarManager manager = Activator.getService(ILaunchBarManager.class);
		uiManager = (LaunchBarUIManager) manager.getAdapter(LaunchBarUIManager.class);
		
		final ILaunchBarManager launchBarManager = Activator.getService(ILaunchBarManager.class); 
		launchBarManager.addListener(new ILaunchBarManager.Listener() {
			@Override
			public void launchTargetsChanged() {
				getCommonViewer().refresh();
			}
			@Override
			public void launchDescriptorRemoved(ILaunchDescriptor descriptor) {
			}
			@Override
			public void activeLaunchTargetChanged() {
			}
			@Override
			public void activeLaunchModeChanged() {
			}
			@Override
			public void activeConfigurationDescriptorChanged() {
			}
		});

	}

	@Override
	protected Object getInitialInput() {
		return uiManager.getManager();
	}
	
}
