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

public class BoardPackage {

	private String name;
	private String maintainer;
	private String websiteURL;
	private String email;
	private Help help;
	private List<Platform> platforms;
	private List<Tool> tools;

	private transient ArduinoBoardManager manager;

	void setOwners(ArduinoBoardManager manager) {
		this.manager = manager;
		for (Platform platform : platforms) {
			platform.setOwners(this);
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

	public Help getHelp() {
		return help;
	}

	public Collection<Platform> getPlatforms() {
		return Collections.unmodifiableCollection(platforms);
	}

	/**
	 * Only the latest versions of the platforms.
	 * 
	 * @return latest platforms
	 */
	public Collection<Platform> getLatestPlatforms() {
		Map<String, Platform> platformMap = new HashMap<>();
		for (Platform platform : platforms) {
			Platform p = platformMap.get(platform.getName());
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

	public Platform getPlatform(String architecture) {
		for (Platform platform : platforms) {
			if (platform.getArchitecture().equals(architecture)) {
				return platform;
			}
		}
		return null;
	}

	public List<Tool> getTools() {
		return tools;
	}

	public Tool getTool(String toolName, String version) {
		for (Tool tool : tools) {
			if (tool.getName().equals(toolName) && tool.getName().equals(version)) {
				return tool;
			}
		}
		return null;
	}

	public void install(IProgressMonitor monitor) {

	}

}
