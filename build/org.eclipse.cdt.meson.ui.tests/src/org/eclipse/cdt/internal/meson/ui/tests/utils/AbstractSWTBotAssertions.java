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

import org.assertj.core.api.AbstractAssert;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;

/**
 * Custom assertions on a given {@link AbstractSWTBot} widget
 * @param <SWTWidget>
 */
public abstract class AbstractSWTBotAssertions<Assertion extends AbstractSWTBotAssertions<Assertion, SWTWidget>, SWTWidget extends AbstractSWTBot<?>>
		extends AbstractAssert<Assertion, SWTWidget> {

	protected AbstractSWTBotAssertions(final SWTWidget actual, final Class<Assertion> clazz) {
		super(actual, clazz);
	}

	@SuppressWarnings("unchecked")
	public Assertion isEnabled() {
		notNullValue();
		if (!actual.isEnabled()) {
			failWithMessage("Expected widget with text '%s (%s)' to be enabled but it was not", actual.getText(),
					actual.getToolTipText());
		}
		return (Assertion) this;
	}

	@SuppressWarnings("unchecked")
	public Assertion isNotEnabled() {
		notNullValue();
		if (actual.isEnabled()) {
			failWithMessage("Expected widget with text '%s (%s)' to be disabled but it was not", actual.getText(),
					actual.getToolTipText());
		}
		return (Assertion) this;
	}

}
