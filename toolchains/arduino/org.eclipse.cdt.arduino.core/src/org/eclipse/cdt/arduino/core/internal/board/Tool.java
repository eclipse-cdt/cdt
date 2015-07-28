package org.eclipse.cdt.arduino.core.internal.board;

import java.util.List;

public class Tool {

	private String name;
	private String version;
	private List<ToolSystem> systems;

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public List<ToolSystem> getSystems() {
		return systems;
	}

}
