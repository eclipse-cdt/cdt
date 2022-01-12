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

import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;

/**
 * Custom assertions on a given {@link SWTBotCheckBox}.
 */
public class CheckBoxAssertions extends AbstractSWTBotAssertions<CheckBoxAssertions, SWTBotCheckBox> {

	protected CheckBoxAssertions(final SWTBotCheckBox actual) {
		super(actual, CheckBoxAssertions.class);
	}

	public static CheckBoxAssertions assertThat(final SWTBotCheckBox actual) {
		return new CheckBoxAssertions(actual);
	}

	public CheckBoxAssertions isChecked() {
		notNullValue();
		if (!actual.isChecked()) {
			failWithMessage("Expected checkbox with text '%s' to be checked but it was not", actual.getText());
		}
		return this;
	}

	public CheckBoxAssertions isNotChecked() {
		notNullValue();
		if (actual.isChecked()) {
			failWithMessage("Expected checkbox with text '%s' to be unchecked but it was not", actual.getText());
		}
		return this;
	}

}
