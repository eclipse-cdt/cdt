/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.board;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

public class ArduinoPackage {

	private String name;
	private String maintainer;
	private String websiteURL;
	private String email;
	private ArduinoHelp help;
	private List<ArduinoPlatform> platforms;
	private List<ArduinoTool> tools;

	private transient ArduinoBoardManager manager;

	void setOwner(ArduinoBoardManager manager) {
		this.manager = manager;
		for (ArduinoPlatform platform : platforms) {
			platform.setOwner(this);
		}
		for (ArduinoTool tool : tools) {
			tool.setOwner(this);
		}
	}

	ArduinoBoardManager getManager() {
		return manager;
	}

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

	/**
	 * Only the latest versions of the platforms.
	 * 
	 * @return latest platforms
	 */
	public Collection<ArduinoPlatform> getLatestPlatforms() {
		Map<String, ArduinoPlatform> platformMap = new HashMap<>();
		for (ArduinoPlatform platform : platforms) {
			ArduinoPlatform p = platformMap.get(platform.getName());
			if (p == null || compareVersions(platform.getVersion(), p.getVersion()) > 0) {
				platformMap.put(platform.getName(), platform);
			}
		}

		return Collections.unmodifiableCollection(platformMap.values());
	}

	private int compareVersions(String version1, String version2) {
		if (version1 == null) {
			return version2 == null ? 0 : -1;
		}

		if (version2 == null) {
			return 1;
		}

		String[] v1 = version1.split("\\."); //$NON-NLS-1$
		String[] v2 = version2.split("\\."); //$NON-NLS-1$
		for (int i = 0; i < Math.max(v1.length, v2.length); ++i) {
			if (v1.length <= i) {
				return v2.length < i ? 0 : -1;
			}

			if (v2.length <= i) {
				return 1;
			}

			try {
				int vi1 = Integer.parseInt(v1[i]);
				int vi2 = Integer.parseInt(v2[i]);
				if (vi1 < vi2) {
					return -1;
				}

				if (vi1 > vi2) {
					return 1;
				}
			} catch (NumberFormatException e) {
				// not numbers, do string compares
				int c = v1[i].compareTo(v2[i]);
				if (c < 0) {
					return -1;
				}
				if (c > 0) {
					return 1;
				}
			}
		}

		return 0;
	}

	public ArduinoPlatform getPlatform(String name) {
		ArduinoPlatform foundPlatform = null;
		for (ArduinoPlatform platform : platforms) {
			if (platform.getName().equals(name)) {
				if (foundPlatform == null) {
					foundPlatform = platform;
				} else {
					if (platform.isInstalled()
							&& compareVersions(platform.getVersion(), foundPlatform.getVersion()) > 0) {
						foundPlatform = platform;
					}
				}
			}
		}
		return foundPlatform;
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

	public void install(IProgressMonitor monitor) {

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
