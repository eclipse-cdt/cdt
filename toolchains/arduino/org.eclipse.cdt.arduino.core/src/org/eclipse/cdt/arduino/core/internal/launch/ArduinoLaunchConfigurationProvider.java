/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.launchbar.core.ProjectLaunchConfigurationProvider;

public class ArduinoLaunchConfigurationProvider extends ProjectLaunchConfigurationProvider {

	@Override
	public ILaunchConfigurationType getLaunchConfigurationType() throws CoreException {
		return ArduinoLaunchConfigurationDelegate.getLaunchConfigurationType();
	}

}
