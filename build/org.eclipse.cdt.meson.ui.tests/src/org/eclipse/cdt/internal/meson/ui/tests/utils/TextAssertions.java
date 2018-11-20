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

import static org.hamcrest.Matchers.notNullValue;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

/**
 * Custom assertions on an {@link SWTBotText}.
 */
public class TextAssertions extends AbstractSWTBotAssertions<TextAssertions, SWTBotText> {

	protected TextAssertions(final SWTBotText actual) {
		super(actual, TextAssertions.class);
	}

	public static TextAssertions assertThat(final SWTBotText actual) {
		return new TextAssertions(actual);
	}

	public TextAssertions isEmpty() {
		notNullValue();
		if (!actual.getText().isEmpty()) {
			failWithMessage("Expected text widget to be empty but it contained '%s'", actual.getText());
		}
		return this;
	}

	public TextAssertions textEquals(final String expectedContent) {
		notNullValue();
		if (!actual.getText().equals(expectedContent)) {
			failWithMessage("Expected text widget to contain '%s' but it contained '%s'", expectedContent,
					actual.getText());
		}
		return this;
	}

}
