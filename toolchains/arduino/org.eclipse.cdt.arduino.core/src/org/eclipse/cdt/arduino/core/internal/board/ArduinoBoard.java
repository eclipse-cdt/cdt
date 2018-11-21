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

	public HierarchicalProperties getMenus() {
		return properties.getChild("menu"); //$NON-NLS-1$
	}

	public Properties getBoardProperties() {
		return properties.flatten();
	}

	public Properties getMenuProperties(String id, String value) {
		return getMenus().getChild(id).getChild(value).flatten();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((platform == null) ? 0 : platform.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArduinoBoard other = (ArduinoBoard) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (platform == null) {
			if (other.platform != null)
				return false;
		} else if (!platform.equals(other.platform))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String arch = getPlatform().getArchitecture();
		String pkg = getPlatform().getPackage().getName();
		return pkg + ',' + arch + ',' + id + ',' + name;
	}

}
