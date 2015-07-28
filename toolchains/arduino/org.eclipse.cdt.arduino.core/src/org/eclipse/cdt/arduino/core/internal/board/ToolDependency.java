/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.board;

public class ToolDependency {

	private String packager;
	private String name;
	private String version;

	public String getPackager() {
		return packager;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

}
