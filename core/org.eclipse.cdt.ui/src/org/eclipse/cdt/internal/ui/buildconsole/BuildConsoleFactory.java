/*******************************************************************************
 * Copyright (c) 2010, 2011 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IBuildConsoleManager;
import org.eclipse.ui.console.IConsoleFactory;

/**
 * A console factory to allow the user to open the build console
 * before actually doing a build.
 */
public class BuildConsoleFactory implements IConsoleFactory {
	@Override
	public void openConsole() {
		IBuildConsoleManager manager = CUIPlugin.getDefault().getConsoleManager();
		if (manager instanceof BuildConsoleManager) {
			((BuildConsoleManager) manager).showConsole(true);
		}
	}

}
