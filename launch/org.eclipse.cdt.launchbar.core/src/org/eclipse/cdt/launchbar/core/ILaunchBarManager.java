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
package org.eclipse.cdt.launchbar.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunchMode;

public interface ILaunchBarManager extends IAdaptable {

	ILaunchConfigurationDescriptor[] getLaunchConfigurationDescriptors();

	ILaunchConfigurationDescriptor getActiveLaunchConfigurationDescriptor();

	void setActiveLaunchConfigurationDescriptor(ILaunchConfigurationDescriptor configDesc) throws CoreException;
	
	void addLaunchConfigurationDescriptor(ILaunchConfigurationDescriptor configDesc) throws CoreException;
	
	void removeLaunchConfigurationDescriptor(ILaunchConfigurationDescriptor configDesc);

	ILaunchMode[] getLaunchModes() throws CoreException;
	
	ILaunchMode getActiveLaunchMode();
	
	void setActiveLaunchMode(ILaunchMode mode);
	
	ILaunchTarget[] getLaunchTargets();
	
	ILaunchTarget getActiveLaunchTarget();
	
	void setActiveLaunchTarget(ILaunchTarget target);
	
	void addLaunchTarget(ILaunchTarget target);
	
	void removeLaunchTarget(ILaunchTarget target);

	interface Listener {

		void activeConfigurationDescriptorChanged();

		void activeLaunchModeChanged();
		
		void activeLaunchTargetChanged();

	}

	void addListener(Listener listener);
	
	void removeListener(Listener listener);

}
