/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
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
