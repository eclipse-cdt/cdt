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
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchMode;

public interface ILaunchBarManager extends IAdaptable {

	ILaunchDescriptor[] getLaunchDescriptors() throws CoreException;

	ILaunchDescriptor getActiveLaunchDescriptor() throws CoreException;

	void setActiveLaunchDescriptor(ILaunchDescriptor configDesc) throws CoreException;
	
	void updateActiveLaunchDescriptor() throws CoreException;

	ILaunchMode[] getLaunchModes() throws CoreException;

	ILaunchMode getActiveLaunchMode() throws CoreException;

	void setActiveLaunchMode(ILaunchMode mode) throws CoreException;

	ILaunchTarget[] getLaunchTargets() throws CoreException;
	
	ILaunchTarget getLaunchTarget(String id) throws CoreException;

	ILaunchTarget getActiveLaunchTarget() throws CoreException;

	void setActiveLaunchTarget(ILaunchTarget target) throws CoreException;
	
	void updateActiveLaunchTarget() throws CoreException;

	ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException;

	ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException;

	ILaunchDescriptor launchObjectAdded(Object element) throws CoreException;

	void launchObjectRemoved(Object element) throws CoreException;

	interface Listener {

		void activeConfigurationDescriptorChanged();

		void activeLaunchModeChanged();

		void activeLaunchTargetChanged();

		void launchDescriptorRemoved(ILaunchDescriptor descriptor);

	}

	void addListener(Listener listener);

	void removeListener(Listener listener);

}
