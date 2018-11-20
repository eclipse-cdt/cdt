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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.arduino.core.internal.ArduinoPreferences;
import org.eclipse.core.runtime.CoreException;

public class ArduinoPackage {

	// JSON fields
	private String name;
	private String maintainer;
	private String websiteURL;
	private String email;
	private ArduinoHelp help;
	private List<ArduinoPlatform> platforms;
	private List<ArduinoTool> tools;
	// end JSON fields

	private Map<String, ArduinoPlatform> installedPlatforms;
	private Map<String, ArduinoTool> latestTools;

	public String getName() {
		return name;
	}

	public String getMaintainer() {
		return maintainer;
	}

	public String getWebsiteURL() {
		return websiteURL;
	}

	public String getEmail() {
		return email;
	}

	public ArduinoHelp getHelp() {
		return help;
	}

	public Collection<ArduinoPlatform> getPlatforms() {
		return Collections.unmodifiableCollection(platforms);
	}

	void init() {
		for (ArduinoPlatform platform : platforms) {
			platform.init(this);
		}
		for (ArduinoTool tool : tools) {
			tool.init(this);
		}
	}

	void merge(ArduinoPackage other) {
		// Redo calculated fields
		installedPlatforms = null;
		latestTools = null;

		if (other.platforms != null) {
			if (platforms != null) {
				platforms.addAll(other.platforms);
			} else {
				platforms = other.platforms;
			}
			for (ArduinoPlatform platform : other.platforms) {
				platform.init(this);
			}
		}

		if (other.tools != null) {
			if (tools != null) {
				tools.addAll(other.tools);
			} else {
				tools = other.tools;
			}
			for (ArduinoTool tool : other.tools) {
				tool.init(this);
			}
		}
	}

	public ArduinoPlatform getPlatform(String architecture, String version) {
		if (platforms != null) {
			for (ArduinoPlatform plat : platforms) {
				if (plat.getArchitecture().equals(architecture) && plat.getVersion().equals(version)) {
					return plat;
				}
			}
		}
		return null;
	}

	public Path getInstallPath() {
		return ArduinoPreferences.getArduinoHome().resolve("packages").resolve(getName()); //$NON-NLS-1$
	}

	private synchronized void initInstalledPlatforms() throws CoreException {
		if (installedPlatforms == null) {
			installedPlatforms = new HashMap<>();

			Path hardware = getInstallPath().resolve("hardware"); //$NON-NLS-1$
			if (Files.isDirectory(hardware)) {
				for (ArduinoPlatform platform : platforms) {
					String arch = platform.getArchitecture();
					String version = platform.getVersion();

					Path platPath = hardware.resolve(arch).resolve(version);
					if (Files.exists(platPath)) {
						ArduinoPlatform current = installedPlatforms.get(arch);
						if (current == null || ArduinoManager.compareVersions(version, current.getVersion()) > 0) {
							installedPlatforms.put(arch, platform);
						}
					}
				}
			}
		}
	}

	public Collection<ArduinoPlatform> getInstalledPlatforms() throws CoreException {
		initInstalledPlatforms();
		return installedPlatforms.values();
	}

	public ArduinoPlatform getInstalledPlatform(String architecture) throws CoreException {
		if (architecture == null) {
			return null;
		} else {
			initInstalledPlatforms();
			return installedPlatforms.get(architecture);
		}
	}

	void platformInstalled(ArduinoPlatform platform) {
		installedPlatforms.put(platform.getArchitecture(), platform);
	}

	void platformUninstalled(ArduinoPlatform platform) {
		installedPlatforms.remove(platform.getArchitecture());
	}

	public Collection<ArduinoPlatform> getAvailablePlatforms() throws CoreException {
		initInstalledPlatforms();
		Map<String, ArduinoPlatform> platformMap = new HashMap<>();
		for (ArduinoPlatform platform : platforms) {
			if (!installedPlatforms.containsKey(platform.getArchitecture())) {
				ArduinoPlatform p = platformMap.get(platform.getArchitecture());
				if (p == null || ArduinoManager.compareVersions(platform.getVersion(), p.getVersion()) > 0) {
					platformMap.put(platform.getArchitecture(), platform);
				}
			}
		}
		return platformMap.values();
	}

	public Collection<ArduinoPlatform> getPlatformUpdates() throws CoreException {
		initInstalledPlatforms();
		Map<String, ArduinoPlatform> platformMap = new HashMap<>();
		for (ArduinoPlatform platform : platforms) {
			ArduinoPlatform installed = installedPlatforms.get(platform.getArchitecture());
			if (installed != null
					&& ArduinoManager.compareVersions(platform.getVersion(), installed.getVersion()) > 0) {
				ArduinoPlatform current = platformMap.get(platform.getArchitecture());
				if (current == null
						|| ArduinoManager.compareVersions(platform.getVersion(), current.getVersion()) > 0) {
					platformMap.put(platform.getArchitecture(), platform);
				}
			}
		}
		return platformMap.values();
	}

	public List<ArduinoTool> getTools() {
		return tools;
	}

	public ArduinoTool getTool(String toolName, String version) {
		for (ArduinoTool tool : tools) {
			if (tool.getName().equals(toolName) && tool.getVersion().equals(version)) {
				return tool;
			}
		}
		return null;
	}

	private void initLatestTools() {
		if (latestTools == null) {
			latestTools = new HashMap<>();

			for (ArduinoTool tool : tools) {
				ArduinoTool current = latestTools.get(tool.getName());
				if (current == null || ArduinoManager.compareVersions(tool.getVersion(), current.getVersion()) > 0) {
					latestTools.put(tool.getName(), tool);
				}
			}
		}
	}

	public ArduinoTool getLatestTool(String toolName) {
		initLatestTools();
		return latestTools.get(toolName);
	}

	public Collection<ArduinoTool> getLatestTools() {
		initLatestTools();
		return latestTools.values();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ArduinoPackage) {
			return ((ArduinoPackage) obj).getName().equals(getName());
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

}
