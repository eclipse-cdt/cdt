package org.eclipse.cdt.qt.core;

public class QMLTernCompletion {

	private final String name;
	private final String type;
	private final String origin;

	public QMLTernCompletion(String name, String type, String origin) {
		this.name = name;
		this.type = type;
		this.origin = origin;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getOrigin() {
		return origin;
	}

}
