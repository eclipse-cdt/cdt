/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial
 *     IBM and others who contributed to ILaunchConfigurationDelegate2
 *         and ILaunchConfigurationDelegate
 *******************************************************************************/
package org.eclipse.launchbar.core.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.remote.core.IRemoteConnection;

/**
 * A launch configuration delegate that accepts a IRemoteConnection as an additional
 * parameter to the launch functions. Delegates who want to receive this parameter from
 * the LaunchBar launch actions need to implement this interface.
 */
public interface IRemoteLaunchConfigurationDelegate extends ILaunchConfigurationDelegate2 {

	/**
	 * Returns a launch object to use when launching the given launch
	 * configuration in the given mode, or <code>null</code> if a new default
	 * launch object should be created by the debug platform. If a launch object
	 * is returned, its launch mode must match that of the mode specified in
	 * this method call.
	 *  
	 * @param configuration the configuration being launched
	 * @param mode the mode the configuration is being launched in
	 * @param target the remote connection to launch on
	 * @return a launch object or <code>null</code>
	 * @throws CoreException if unable to launch
	 */
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode, IRemoteConnection target) throws CoreException;

	/**
	 * Optionally performs any required building before launching the given
	 * configuration in the specified mode, and returns whether the debug platform
	 * should perform an incremental workspace build before the launch continues.
	 * If <code>false</code> is returned the launch will proceed without further
	 * building, and if <code>true</code> is returned an incremental build will
	 * be performed on the workspace before launching.
	 * <p>
	 * This method is only called if the launch is invoked with flag indicating
	 * building should take place before the launch. This is done via the    
	 * method
	 * <code>ILaunchConfiguration.launch(String mode, IProgressMonitor monitor, boolean build)</code>.
	 * </p> 
	 * @param configuration the configuration being launched
	 * @param mode the mode the configuration is being launched in
	 * @param target the remote connection the configuration is being launched on
	 * @param monitor progress monitor, or <code>null</code>. A cancelable progress monitor is provided by the Job
	 *  framework. It should be noted that the setCanceled(boolean) method should never be called on the provided
	 *  monitor or the monitor passed to any delegates from this method; due to a limitation in the progress monitor 
	 *  framework using the setCanceled method can cause entire workspace batch jobs to be canceled, as the canceled flag 
	 *  is propagated up the top-level parent monitor. The provided monitor is not guaranteed to have been started. 
	 * @return whether the debug platform should perform an incremental workspace
	 *  build before the launch
	 * @throws CoreException if an exception occurs while building
	 */
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IRemoteConnection target, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns whether a launch should proceed. This method is called after
	 * <code>preLaunchCheck()</code> and <code>buildForLaunch()</code> providing
	 * a final chance for this launch delegate to abort a launch if required.
	 * For example, a delegate could cancel a launch if it discovered compilation
	 * errors that would prevent the launch from succeeding.
	 * 
	 * @param configuration the configuration being launched
	 * @param mode launch mode
	 * @param target the remote connection the configuration is being launched on
	 * @param monitor progress monitor, or <code>null</code>. A cancelable progress monitor is provided by the Job
	 *  framework. It should be noted that the setCanceled(boolean) method should never be called on the provided
	 *  monitor or the monitor passed to any delegates from this method; due to a limitation in the progress monitor 
	 *  framework using the setCanceled method can cause entire workspace batch jobs to be canceled, as the canceled flag 
	 *  is propagated up the top-level parent monitor. The provided monitor is not guaranteed to have been started. 
	 * @return whether the launch should proceed
	 * @throws CoreException if an exception occurs during final checks
	 */
	public boolean finalLaunchCheck(ILaunchConfiguration configuration, String mode, IRemoteConnection target, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns whether a launch should proceed. This method is called first
	 * in the launch sequence providing an opportunity for this launch delegate
	 * to abort the launch.
	 * 
	 * @param configuration configuration being launched
	 * @param mode launch mode
	 * @param target the remote connection the configuration is being launched on
	 * @param monitor progress monitor, or <code>null</code>. A cancelable progress monitor is provided by the Job
	 *  framework. It should be noted that the setCanceled(boolean) method should never be called on the provided
	 *  monitor or the monitor passed to any delegates from this method; due to a limitation in the progress monitor 
	 *  framework using the setCanceled method can cause entire workspace batch jobs to be canceled, as the canceled flag 
	 *  is propagated up the top-level parent monitor. The provided monitor is not guaranteed to have been started. 
	 * @return whether the launch should proceed
	 * @throws CoreException if an exception occurs while performing pre-launch checks
	 */
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IRemoteConnection target, IProgressMonitor monitor) throws CoreException;

	/**
	 * Launches the given configuration in the specified mode, contributing
	 * debug targets and/or processes to the given launch object. The
	 * launch object has already been registered with the launch manager.
	 * 
	 * @param configuration the configuration to launch
	 * @param mode the mode in which to launch, one of the mode constants
	 *  defined by <code>ILaunchManager</code> -
	 *  <code>RUN_MODE</code> or <code>DEBUG_MODE</code>.
	 * @param target the remote connection the configuration to launched on
	 * @param monitor progress monitor, or <code>null</code> progress monitor, or <code>null</code>. A cancelable progress 
	 * monitor is provided by the Job framework. It should be noted that the setCanceled(boolean) method should 
	 * never be called on the provided monitor or the monitor passed to any delegates from this method; due to a 
	 * limitation in the progress monitor framework using the setCanceled method can cause entire workspace batch 
	 * jobs to be canceled, as the canceled flag is propagated up the top-level parent monitor. 
	 * The provided monitor is not guaranteed to have been started. 
	 * @param launch the launch object to contribute processes and debug
	 *  targets to
	 * @exception CoreException if launching fails 
	 */
	public void launch(ILaunchConfiguration configuration, String mode, IRemoteConnection target, ILaunch launch, IProgressMonitor monitor) throws CoreException;

}
