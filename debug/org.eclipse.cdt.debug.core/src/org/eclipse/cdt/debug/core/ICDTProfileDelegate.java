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
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

/**
 * @since 7.3
 */
public interface ICDTProfileDelegate extends ILaunchConfigurationDelegate {
	/**
	 * Set default profiling attributes for a new configuration
	 * 
	 * @param wc launch configuration working copy
	 */
	public void setDefaultProfileLaunchAttributes(ILaunchConfigurationWorkingCopy wc);
}
