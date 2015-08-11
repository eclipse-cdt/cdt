/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.board;

import java.util.Properties;

import org.eclipse.cdt.arduino.core.internal.HierarchicalProperties;

public class ArduinoBoard {

	private String name;

	private String id;

	private ArduinoPlatform platform;
	private HierarchicalProperties properties;

	public ArduinoBoard() {
	}

	public ArduinoBoard(String id, HierarchicalProperties properties) {
		this.properties = properties;
		this.id = id;
		this.name = this.properties.getChild("name").getValue(); //$NON-NLS-1$
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public ArduinoPlatform getPlatform() {
		return platform;
	}

	ArduinoBoard setOwners(ArduinoPlatform platform) {
		this.platform = platform;
		return this;
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public Properties getBoardProperties() {
		return properties.flatten();
	}

}
