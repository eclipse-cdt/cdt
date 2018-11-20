/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.cdt.internal.meson.ui.tests.utils;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.ui.console.IConsoleConstants;
import org.junit.rules.ExternalResource;

/**
 * An {@link ExternalResource} to close the Console view.
 */
public class ConsoleViewRule extends ExternalResource {

	@Override
	protected void before() {
		Display.getDefault().syncExec(() -> {
			final SWTBotView consoleView = SWTUtils.getSWTBotView(new SWTWorkbenchBot(),
					IConsoleConstants.ID_CONSOLE_VIEW);
			if (consoleView != null) {
				consoleView.close();
			}
		});
	}

}
