/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.cdt.launchbar.ui.internal.commands;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchConfigurationDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.cdt.launchbar.ui.internal.Activator;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

public class LaunchActiveCommandHandler extends AbstractHandler {

	private final ILaunchBarManager launchBarManager;
	
	public LaunchActiveCommandHandler() {
		launchBarManager = Activator.getService(ILaunchBarManager.class);
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		new UIJob(Display.getDefault(), "Launching Active Configuration") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				try {
					ILaunchConfigurationDescriptor desc = launchBarManager.getActiveLaunchConfigurationDescriptor();

					if (desc == null) {
						// popout - No launch configuration
//						showConfigurationErrorCallOut(NLS.bind("{0}\n{1}", Messages.RunActiveCommandHandler_No_Launch_Configuration_Selected, 
//								Messages.RunActiveCommandHanlder_Create_Launch_Configuration));
						return Status.OK_STATUS;
					}

					ILaunchMode launchMode = launchBarManager.getActiveLaunchMode();
					ILaunchTarget target = launchBarManager.getActiveLaunchTarget();

					if (target == null) {
						// popout - No target
//						if (TargetCorePlugin.getDefault().getTargetRegistry().getTargets().length == 1) {
//							showTargetErrorCallOut(NLS.bind("{0}{1}", Messages.RunActiveCommandHandler_Cannot, getMode(launchMode)),
//									DeviceErrors.Error.NO_DEVICES, Messages.RunActiveCommandHandler_Select_Manage_Devices);
//						} else { // Don't think this can occur. Leaving just in case.
//							showTargetErrorCallOut(NLS.bind("{0}{1}", Messages.RunActiveCommandHandler_Cannot, getMode(launchMode)),
//									DeviceErrors.Error.NO_ACTIVE, Messages.RunActiveCommandHandler_Select_Device_Or_Simulator);
//						}
						return Status.OK_STATUS;
					}

					DebugUITools.launch(desc.getLaunchConfiguration(), launchMode.getIdentifier());
				} catch (CoreException e) {
					return e.getStatus();
				}
				return Status.OK_STATUS;
			};
		}.schedule();

		return Status.OK_STATUS;
	}

	protected void showConfigurationErrorCallOut(final String error) {
//		Control activeProjectControl = LaunchToolBar.getInstance().getActiveConfigurationControl();
//		showErrorToolTipOnControl(error, activeProjectControl);
	}

//	protected void showTargetErrorCallOut(final String title, DeviceErrors.Error e, String action) {
//		Control activeTargetControl = LaunchToolBar.getInstance().getActiveTargetControl();
//		DeviceErrors.showCallOutErrorOnControl(activeTargetControl, title, e, action);
//	}

	public static void showErrorToolTipOnControl(final String error, Control activeProjectControl) {
//		CalloutError tip = new CalloutError();
//		tip.setTarget(activeProjectControl);
//		tip.setHook(Position.BOTTOM_CENTER, Position.TOP_CENTER, new Point(0, 0));
//		tip.setData(error);
//		tip.show();
	}

	protected String getMode(ILaunchMode launchMode) {
		return launchMode.getIdentifier(); //$NON-NLS-1$
	}

}
