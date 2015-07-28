/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.board;

import org.eclipse.cdt.arduino.core.internal.HierarchicalProperties;

public class Board {

	private String name;

	private String id;

	private Platform platform;
	private HierarchicalProperties properties;

	public Board() {
	}

	public Board(HierarchicalProperties properties) {
		this.properties = properties;
		this.id = this.properties.getValue();
		this.name = this.properties.getChild("name").getValue(); //$NON-NLS-1$
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public Platform getPlatform() {
		return platform;
	}

	Board setOwners(Platform platform) {
		this.platform = platform;
		return this;
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public String getPlatformId() {
		return platform.getArchitecture();
	}

	public String getPackageId() {
		return platform.getPackage().getName();
	}

}
