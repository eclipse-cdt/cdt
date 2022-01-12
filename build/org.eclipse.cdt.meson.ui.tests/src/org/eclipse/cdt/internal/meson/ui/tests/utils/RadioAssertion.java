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

import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;

/**
 * Custom assertions on a given {@link SWTBotRadio}.
 */
public class RadioAssertion extends AbstractSWTBotAssertions<RadioAssertion, SWTBotRadio> {

	protected RadioAssertion(final SWTBotRadio actual) {
		super(actual, RadioAssertion.class);
	}

	public static RadioAssertion assertThat(final SWTBotRadio actual) {
		return new RadioAssertion(actual);
	}

	public RadioAssertion isSelected() {
		notNullValue();
		if (!actual.isSelected()) {
			failWithMessage("Expected checkbox with text '%s' to be checked but it was not", actual.getText());
		}
		return this;
	}

	public RadioAssertion isNotSelected() {
		notNullValue();
		if (actual.isSelected()) {
			failWithMessage("Expected checkbox with text '%s' to be unchecked but it was not", actual.getText());
		}
		return this;
	}

}
