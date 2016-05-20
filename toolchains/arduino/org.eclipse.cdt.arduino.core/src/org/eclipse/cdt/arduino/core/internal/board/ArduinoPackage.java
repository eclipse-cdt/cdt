/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.board;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.arduino.core.internal.Activator;
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

	private void initInstalledPlatforms() throws CoreException {
		if (installedPlatforms == null) {
			installedPlatforms = new HashMap<>();

			if (Files.isDirectory(getInstallPath())) {
				Path platformTxt = Paths.get("platform.txt"); //$NON-NLS-1$
				try {
					Files.find(getInstallPath().resolve("hardware"), 2, //$NON-NLS-1$
							(path, attrs) -> path.getFileName().equals(platformTxt))
							.forEach(path -> {
								try (FileReader reader = new FileReader(path.toFile())) {
									Properties platformProperties = new Properties();
									platformProperties.load(reader);
									String arch = path.getName(path.getNameCount() - 2).toString();
									String version = platformProperties.getProperty("version"); //$NON-NLS-1$

									ArduinoPlatform platform = getPlatform(arch, version);
									if (platform != null) {
										platform.setPlatformProperties(platformProperties);
										installedPlatforms.put(arch, platform);
									} // TODO manually add it if was removed from index
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
							});
				} catch (IOException e) {
					throw Activator.coreException(e);
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
				ArduinoPlatform p = platformMap.get(platform.getName());
				if (p == null || ArduinoManager.compareVersions(platform.getVersion(), p.getVersion()) > 0) {
					platformMap.put(platform.getName(), platform);
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

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ArduinoPackage) {
			return ((ArduinoPackage) obj).getName().equals(name);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

}
