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
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;

/**
 * Custom assertions on a given {@link SWTBotCheckBox}.
 */
public class ComboAssertions extends AbstractSWTBotAssertions<ComboAssertions, SWTBotCombo> {

	protected ComboAssertions(final SWTBotCombo actual) {
		super(actual, ComboAssertions.class);
	}

	public static ComboAssertions assertThat(final SWTBotCombo actual) {
		return new ComboAssertions(actual);
	}

	public ComboAssertions itemSelected(final String expectedItem) {
		notNullValue();
		if (actual.selectionIndex() < 0) {
			failWithMessage("Expected combo to have selection to '%s' but it had none", expectedItem);
		} else if (!actual.selection().equals(expectedItem)) {
			failWithMessage("Expected combo to have selection to '%s' but it was '%s'", expectedItem,
					actual.selection());
		}
		return this;
	}

	public ComboAssertions indexItemSelected(final int expectedItemIndex) {
		notNullValue();
		if (actual.selectionIndex() < 0) {
			failWithMessage("Expected combo to have selection index to '%s' but it had none", expectedItemIndex);
		} else if (actual.selectionIndex() != expectedItemIndex) {
			failWithMessage("Expected combo to have selection index to '%s' but it was '%s'", expectedItemIndex,
					actual.selectionIndex());
		}
		return this;
	}

}
