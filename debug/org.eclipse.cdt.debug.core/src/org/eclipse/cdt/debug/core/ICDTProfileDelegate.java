/*******************************************************************************
 * Copyright (c) 2012 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;

/**
 * @since 7.3
 */
public interface ICDTProfileDelegate extends ILaunchConfigurationDelegate2 {
	/**
	 * Set default attributes for a new profiling configuration such
	 * as default parameters to a particular profiling tool or special
	 * attributes that are needed to identify the provider.  This call must
	 * at the very least set the attribute: "org.eclipse.cdt.launch.profilingProvider" to
	 * something other than the empty string.  This is required so older C Run/Debug
	 * launch configurations can be identified as having no profiling attributes set
	 * up and so this call can be made prior to launching for profile.
	 * 
	 * Default attributes should only be set once as the user is free to modify values
	 * for the configuration after initial set-up.
	 * 
	 * @param wc launch configuration working copy
	 */
	public void setDefaultProfileLaunchAttributes(ILaunchConfigurationWorkingCopy wc);
}
