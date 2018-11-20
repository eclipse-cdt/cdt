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
package org.eclipse.cdt.arduino.core.internal.board;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class ToolDependency {

	private String packager;
	private String name;
	private String version;

	public String getPackager() {
		return packager;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version.replace('+', '_');
	}

	public ArduinoTool getTool() throws CoreException {
		return Activator.getService(ArduinoManager.class).getTool(getPackager(), getName(), getVersion());
	}

	public void install(IProgressMonitor monitor) throws CoreException {
		ArduinoTool tool = getTool();
		if (tool == null) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(),
					String.format("Tool not found %s %s", getName(), getVersion())));
		}
		getTool().install(monitor);
	}

}
