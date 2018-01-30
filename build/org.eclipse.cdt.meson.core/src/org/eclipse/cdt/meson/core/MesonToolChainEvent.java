/*******************************************************************************
 * Copyright (c) 2016, 2018 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Red Hat Inc. - modified for use with Meson builder
 *******************************************************************************/
package org.eclipse.cdt.meson.core;

/**
 * Event occured with Meson ToolChain Files, either added or removed.
 */
public class MesonToolChainEvent {

	/**
	 * ToolChain file has been added.
	 */
	public static final int ADDED = 1;

	/**
	 * ToolChain File has been removed.
	 */
	public static final int REMOVED = 2;

	private final int type;
	private final IMesonToolChainFile toolChainFile;

	public MesonToolChainEvent(int type, IMesonToolChainFile toolChainFile) {
		this.type = type;
		this.toolChainFile = toolChainFile;
	}

	public int getType() {
		return type;
	}

	public IMesonToolChainFile getToolChainFile() {
		return toolChainFile;
	}

}
