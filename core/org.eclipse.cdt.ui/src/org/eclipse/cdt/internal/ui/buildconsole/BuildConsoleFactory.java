/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.ui.console.IConsoleFactory;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IBuildConsoleManager;

/**
 * A console factory to allow the user to open the build console
 * before actually doing a build.
 */
public class BuildConsoleFactory implements IConsoleFactory {
	public void openConsole() {
		IBuildConsoleManager manager = CUIPlugin.getDefault().getConsoleManager();
		if (manager instanceof BuildConsoleManager) {
			((BuildConsoleManager)manager).showConsole();
		}
	}

}
