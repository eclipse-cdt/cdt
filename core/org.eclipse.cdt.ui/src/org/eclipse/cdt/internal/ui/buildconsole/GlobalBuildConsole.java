/*******************************************************************************
 * Copyright (c) 2010, 2011 Broadcom Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Alex Collins (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import java.net.URL;

import org.eclipse.cdt.ui.IBuildConsoleManager;
import org.eclipse.core.resources.IProject;

/**
 * Customized BuildConsole for the global console that displays its title differently
 */
public class GlobalBuildConsole extends BuildConsole {
	public GlobalBuildConsole(IBuildConsoleManager manager, String name, String contextId, URL iconUrl) {
		super(manager, name, contextId, iconUrl);
	}

	@Override
	public void setTitle(IProject project) {
	}
}
