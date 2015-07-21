package org.eclipse.cdt.arduino.core.board;

public class Board {

	private String name;

	private transient String id;
	private transient Platform platform;

	public String getName() {
		return name;
	}

	public Board setName(String name) {
		this.name = name;
		return this;
	}

	public String getId() {
		return id;
	}

	public Board setId(String id) {
		this.id = id;
		return this;
	}

	public Platform getPlatform() {
		return platform;
	}

	Board setOwners(Platform platform) {
		this.platform = platform;
		return this;
	}

	public String getBuildSetting(String setting) {
		String key = id + ".build." + setting; //$NON-NLS-1$
		return platform.getBoardsFile().getProperty(key);
	}

	public String getPlatformId() {
		return platform.getArchitecture();
	}

	public String getPackageId() {
		return platform.getPackage().getName();
	}

}
