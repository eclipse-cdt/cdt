package org.eclipse.cdt.arduino.core.board;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

public class Package {

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

	public List<Platform> getPlatforms() {
		return platforms;
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

	public void install(IProgressMonitor monitor) {

	}

}
