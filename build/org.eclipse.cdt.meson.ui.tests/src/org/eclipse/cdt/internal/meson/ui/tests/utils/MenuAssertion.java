/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat Inc.
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

import static org.hamcrest.Matchers.notNullValue;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;

/**
 * Custom assertions on a given {@link SWTBotButton}.
 */
public class MenuAssertion extends AbstractSWTBotAssertions<MenuAssertion, SWTBotMenu> {

	protected MenuAssertion(final SWTBotMenu actual) {
		super(actual, MenuAssertion.class);
	}

	public static MenuAssertion assertThat(final SWTBotMenu actual) {
		return new MenuAssertion(actual);
	}

	public MenuAssertion isVisible() {
		notNullValue();
		if (!actual.isVisible()) {
			failWithMessage("Expected menu with text '%s' to be visible but it was not", actual.getText());
		}
		return this;
	}

	public MenuAssertion isNotVisible() {
		notNullValue();
		if (actual.isVisible()) {
			failWithMessage("Expected menu with text '%s' to be visible but it was not", actual.getText());
		}
		return this;
	}
}
