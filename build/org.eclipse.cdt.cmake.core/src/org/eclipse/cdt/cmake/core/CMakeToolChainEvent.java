/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.core;

/**
 * Event occured with CMake ToolChain Files, either added or removed.
 */
public class CMakeToolChainEvent {

	/**
	 * ToolChain file has been added.
	 */
	public static final int ADDED = 1;

	/**
	 * ToolChain File has been removed.
	 */
	public static final int REMOVED = 2;

	private final int type;
	private final ICMakeToolChainFile toolChainFile;

	public CMakeToolChainEvent(int type, ICMakeToolChainFile toolChainFile) {
		this.type = type;
		this.toolChainFile = toolChainFile;
	}

	public int getType() {
		return type;
	}

	public ICMakeToolChainFile getToolChainFile() {
		return toolChainFile;
	}

}
