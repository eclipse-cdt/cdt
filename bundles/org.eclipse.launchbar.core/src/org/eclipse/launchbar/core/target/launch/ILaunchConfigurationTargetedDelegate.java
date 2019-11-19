/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.launchbar.core.target.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.launchbar.core.target.ILaunchTarget;

/**
 * An ILaunchConfigurationDelegate2 converted to take ILaunchTarget as an
 * additional parameter.
 */
public interface ILaunchConfigurationTargetedDelegate extends ILaunchConfigurationDelegate {

	/**
	 * Returns a launch object to use when launching the given launch
	 * configuration in the given mode, or <code>null</code> if a new default
	 * launch object should be created by the debug platform. If a launch object
	 * is returned, its launch mode must match that of the mode specified in
	 * this method call.
	 * 
	 * @param configuration
	 *            the configuration being launched
	 * @param mode
	 *            the mode the configuration is being launched in
	 * @return a launch object or <code>null</code>
	 * @throws CoreException
	 *             if unable to launch
	 */
	public ITargetedLaunch getLaunch(ILaunchConfiguration configuration, String mode, ILaunchTarget target)
			throws CoreException;

	/**
	 * Optionally performs any required building before launching the given
	 * configuration in the specified mode, and returns whether the debug
	 * platform should perform an incremental workspace build before the launch
	 * continues. If <code>false</code> is returned the launch will proceed
	 * without further building, and if <code>true</code> is returned an
	 * incremental build will be performed on the workspace before launching.
	 * <p>
	 * This method is only called if the launch is invoked with flag indicating
	 * building should take place before the launch. This is done via the method
	 * <code>ILaunchConfiguration.launch(String mode, IProgressMonitor monitor, boolean build)</code>
	 * .
	 * </p>
	 * 
	 * @param configuration
	 *            the configuration being launched
	 * @param mode
	 *            the mode the configuration is being launched in
	 * @param monitor
	 *            progress monitor, or <code>null</code>. A cancelable progress
	 *            monitor is provided by the Job framework. It should be noted
	 *            that the setCanceled(boolean) method should never be called on
	 *            the provided monitor or the monitor passed to any delegates
	 *            from this method; due to a limitation in the progress monitor
	 *            framework using the setCanceled method can cause entire
	 *            workspace batch jobs to be canceled, as the canceled flag is
	 *            propagated up the top-level parent monitor. The provided
	 *            monitor is not guaranteed to have been started.
	 * @return whether the debug platform should perform an incremental
	 *         workspace build before the launch
	 * @throws CoreException
	 *             if an exception occurs while building
	 */
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, ILaunchTarget target,
			IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns whether a launch should proceed. This method is called after
	 * <code>preLaunchCheck()</code> and <code>buildForLaunch()</code> providing
	 * a final chance for this launch delegate to abort a launch if required.
	 * For example, a delegate could cancel a launch if it discovered
	 * compilation errors that would prevent the launch from succeeding.
	 * 
	 * @param configuration
	 *            the configuration being launched
	 * @param mode
	 *            launch mode
	 * @param monitor
	 *            progress monitor, or <code>null</code>. A cancelable progress
	 *            monitor is provided by the Job framework. It should be noted
	 *            that the setCanceled(boolean) method should never be called on
	 *            the provided monitor or the monitor passed to any delegates
	 *            from this method; due to a limitation in the progress monitor
	 *            framework using the setCanceled method can cause entire
	 *            workspace batch jobs to be canceled, as the canceled flag is
	 *            propagated up the top-level parent monitor. The provided
	 *            monitor is not guaranteed to have been started.
	 * @return whether the launch should proceed
	 * @throws CoreException
	 *             if an exception occurs during final checks
	 */
	public boolean finalLaunchCheck(ILaunchConfiguration configuration, String mode, ILaunchTarget target,
			IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns whether a launch should proceed. This method is called first in
	 * the launch sequence providing an opportunity for this launch delegate to
	 * abort the launch.
	 * 
	 * @param configuration
	 *            configuration being launched
	 * @param mode
	 *            launch mode
	 * @param monitor
	 *            progress monitor, or <code>null</code>. A cancelable progress
	 *            monitor is provided by the Job framework. It should be noted
	 *            that the setCanceled(boolean) method should never be called on
	 *            the provided monitor or the monitor passed to any delegates
	 *            from this method; due to a limitation in the progress monitor
	 *            framework using the setCanceled method can cause entire
	 *            workspace batch jobs to be canceled, as the canceled flag is
	 *            propagated up the top-level parent monitor. The provided
	 *            monitor is not guaranteed to have been started.
	 * @return whether the launch should proceed
	 * @throws CoreException
	 *             if an exception occurs while performing pre-launch checks
	 */
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, ILaunchTarget target,
			IProgressMonitor monitor) throws CoreException;

}
