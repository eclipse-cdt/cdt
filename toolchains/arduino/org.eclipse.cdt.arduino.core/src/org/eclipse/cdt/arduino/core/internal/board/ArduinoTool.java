/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.board;

import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.build.ArduinoBuildConfiguration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class ArduinoTool {

	private String name;
	private String version;
	private List<ArduinoToolSystem> systems;

	private transient ArduinoPackage pkg;

	public ArduinoPackage getPackage() {
		return pkg;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version.replace('+', '_');
	}

	public List<ArduinoToolSystem> getSystems() {
		return systems;
	}

	void init(ArduinoPackage pkg) {
		this.pkg = pkg;
		for (ArduinoToolSystem system : systems) {
			system.setOwner(this);
		}
	}

	public Path getInstallPath() {
		return getPackage().getInstallPath().resolve("tools").resolve(getName()).resolve(getVersion()); //$NON-NLS-1$
	}

	public boolean isInstalled() {
		return getInstallPath().toFile().exists();
	}

	public void install(IProgressMonitor monitor) throws CoreException {
		if (isInstalled()) {
			return;
		}

		for (ArduinoToolSystem system : systems) {
			if (system.isApplicable()) {
				system.install(monitor);
				return;
			}
		}

		// No valid system
		throw new CoreException(
				new Status(IStatus.ERROR, Activator.getId(), String.format("No valid system found for %s", name))); //$NON-NLS-1$
	}

	public Properties getToolProperties() {
		Properties properties = new Properties();
		properties.put("runtime.tools." + name + ".path", ArduinoBuildConfiguration.pathString(getInstallPath())); //$NON-NLS-1$//$NON-NLS-2$
		properties.put("runtime.tools." + name + '-' + version + ".path", //$NON-NLS-1$//$NON-NLS-2$
				ArduinoBuildConfiguration.pathString(getInstallPath())); //$NON-NLS-1$
		return properties;
	}

}
