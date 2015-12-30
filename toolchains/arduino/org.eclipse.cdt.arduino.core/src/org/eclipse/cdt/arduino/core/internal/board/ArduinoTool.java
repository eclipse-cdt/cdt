/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.board;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.ArduinoPreferences;
import org.eclipse.cdt.arduino.core.internal.build.ArduinoBuildConfiguration;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class ArduinoTool {

	private String name;
	private String version;
	private List<ArduinoToolSystem> systems;

	private transient ArduinoPackage pkg;

	public void setOwner(ArduinoPackage pkg) {
		this.pkg = pkg;
		for (ArduinoToolSystem system : systems) {
			system.setOwner(this);
		}
	}

	public ArduinoPackage getPackage() {
		return pkg;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public List<ArduinoToolSystem> getSystems() {
		return systems;
	}

	public Path getInstallPath() {
		// TODO remove migration in Neon
		Path oldPath = ArduinoPreferences.getArduinoHome().resolve("tools").resolve(pkg.getName()).resolve(name) //$NON-NLS-1$
				.resolve(version);
		Path newPath = getPackage().getInstallPath().resolve("tools").resolve(name).resolve(version); //$NON-NLS-1$
		if (Files.exists(oldPath)) {
			try {
				Files.createDirectories(newPath.getParent());
				Files.move(oldPath, newPath);
				for (Path parent = oldPath.getParent(); parent != null; parent = parent.getParent()) {
					if (Files.newDirectoryStream(parent).iterator().hasNext()) {
						break;
					} else {
						Files.delete(parent);
					}
				}
			} catch (IOException e) {
				Activator.log(e);
			}
		}
		return newPath;
	}

	public boolean isInstalled() {
		return getInstallPath().toFile().exists();
	}

	public IStatus install(IProgressMonitor monitor) {
		if (isInstalled()) {
			return Status.OK_STATUS;
		}

		for (ArduinoToolSystem system : systems) {
			if (system.isApplicable()) {
				return system.install(monitor);
			}
		}

		// No valid system
		return new Status(IStatus.ERROR, Activator.getId(), "No valid system found for " + name); //$NON-NLS-1$
	}

	public Properties getToolProperties() {
		Properties properties = new Properties();
		properties.put("runtime.tools." + name + ".path", ArduinoBuildConfiguration.pathString(getInstallPath())); // $NON-NLS-1$ //$NON-NLS-1$//$NON-NLS-2$
		properties.put("runtime.tools." + name + '-' + version + ".path", //$NON-NLS-1$//$NON-NLS-2$
				ArduinoBuildConfiguration.pathString(getInstallPath())); // $NON-NLS-1$
		return properties;
	}

}
