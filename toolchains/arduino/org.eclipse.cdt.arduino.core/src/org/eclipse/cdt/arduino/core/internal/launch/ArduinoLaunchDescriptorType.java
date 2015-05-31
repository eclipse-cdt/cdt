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

import org.eclipse.cdt.arduino.core.internal.ArduinoProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptorType;
import org.eclipse.launchbar.core.ProjectLaunchDescriptor;

public class ArduinoLaunchDescriptorType implements ILaunchDescriptorType {

	@Override
	public ILaunchDescriptor getDescriptor(Object launchObject) throws CoreException {
		if (launchObject instanceof IProject && ArduinoProjectNature.hasNature((IProject) launchObject)) {
			return new ProjectLaunchDescriptor(this, (IProject) launchObject);
		}

		return null;
	}

}
