/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.internal;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;

public class CMakeToolChainFile implements ICMakeToolChainFile {

	String n;
	private final Path path;

	final Map<String, String> properties = new HashMap<>();

	public CMakeToolChainFile(String n, Path path) {
		this.n = n;
		this.path = path;
	}

	@Override
	public Path getPath() {
		return path;
	}

	@Override
	public String getProperty(String key) {
		return properties.get(key);
	}

	@Override
	public void setProperty(String key, String value) {
		properties.put(key, value);
	}

	boolean matches(Map<String, String> properties) {
		for (Map.Entry<String, String> property : properties.entrySet()) {
			if (!property.getValue().equals(getProperty(property.getKey()))) {
				return false;
			}
		}
		return true;
	}

}
