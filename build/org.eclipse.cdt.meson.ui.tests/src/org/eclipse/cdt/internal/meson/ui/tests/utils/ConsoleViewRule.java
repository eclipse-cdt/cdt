/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
