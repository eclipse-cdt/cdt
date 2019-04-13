/*******************************************************************************
 * Copyright (c) 2016, 2019 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Red Hat Inc. - modified for use with Autotools builder
 *******************************************************************************/
package org.eclipse.cdt.core.autotools.core.internal;

import org.eclipse.cdt.core.autotools.core.IAutotoolsToolChainFile;

/**
 * Event occured with Autotools ToolChain Files, either added or removed.
 */
public class AutotoolsToolChainEvent {

	/**
	 * ToolChain file has been added.
	 */
	public static final int ADDED = 1;

	/**
	 * ToolChain File has been removed.
	 */
	public static final int REMOVED = 2;

	private final int type;
	private final IAutotoolsToolChainFile toolChainFile;

	public AutotoolsToolChainEvent(int type, IAutotoolsToolChainFile toolChainFile) {
		this.type = type;
		this.toolChainFile = toolChainFile;
	}

	public int getType() {
		return type;
	}

	public IAutotoolsToolChainFile getToolChainFile() {
		return toolChainFile;
	}

}
