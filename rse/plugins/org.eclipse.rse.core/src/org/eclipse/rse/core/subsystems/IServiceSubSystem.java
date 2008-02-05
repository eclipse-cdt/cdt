/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David Dykstal (IBM) - [197036] added javadoc and canSwitchTo method.
 *******************************************************************************/

package org.eclipse.rse.core.subsystems;

/**
 * A service subsystem provides a common subsystem implementation defined on a
 * service interface. The service itself may have various
 * implementations determined by a subsystem configuration. A service subsystem
 * may be asked to switch its subsystem configuration in order to switch the
 * underlying service implementation. 
 */
public interface IServiceSubSystem extends ISubSystem {
	
	/**
	 * Returns the interface type (i.e. a Class object that is an Interface) of a service subsystem. 
	 * @return the service interface on which this service subsystem is implemented. If this
	 * subsystem is not a service subsystem it must return null.
	 */
	public Class getServiceType();

	/**
	 * Requests a service subsystem to switch to a new configuration. If the configuration
	 * is compatible with this subsystem then it must disconnect, possibly reset its
	 * filter pool references, and request new services and parameters from its new configuration.
	 * It must also answer true to {@link #canSwitchTo(IServiceSubSystemConfiguration)}.
	 * If the configuration is not compatible with this subsystem then this must do nothing and must answer
	 * false to {@link #canSwitchTo(IServiceSubSystemConfiguration)}.
	 * @param configuration the configuration to which to switch.
	 */
	public void switchServiceFactory(IServiceSubSystemConfiguration configuration);
	
	/**
	 * Determine is this subsystem is compatible with this specified configuration.
	 * @param configuration the configuration which may be switched to
	 * @return true if the subsystem can switch to this configuration, false otherwise.
	 * Subsystems which are not service subsystems must return false.
	 */
	public boolean canSwitchTo(IServiceSubSystemConfiguration configuration);
	
}
