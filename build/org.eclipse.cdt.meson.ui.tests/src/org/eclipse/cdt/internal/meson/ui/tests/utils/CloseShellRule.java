/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
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

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.ui.PlatformUI;
import org.junit.rules.ExternalResource;

/**
 * Closes the wizard(s) after each test, if the "Cancel" button is available
 */
public class CloseShellRule extends ExternalResource {

	private final String buttonLabel;

	public CloseShellRule(final String buttonLabel) {
		this.buttonLabel = buttonLabel;
	}

	@Override
	protected void after() {
		final SWTWorkbenchBot bot = new SWTWorkbenchBot();
		try {
			while (isInDialog(bot) && getButton(bot, this.buttonLabel) != null) {
				getButton(bot, this.buttonLabel).click();
			}

		} catch (WidgetNotFoundException e) {
			// ignoring
		}
	}

	private static boolean isInDialog(final SWTWorkbenchBot bot) {
		final SWTBotShell activeShell = bot.activeShell();
		final String text = SWTUtils
				.syncExec(() -> PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getText());
		final String shellText = activeShell.getText();
		return text != null && !text.equals(shellText);
	}

	private static SWTBotButton getButton(final SWTWorkbenchBot bot, final String buttonLabel) {
		return bot.button(buttonLabel);
	}
}
