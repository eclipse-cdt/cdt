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

import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;

/**
 * Custom assertions on a given {@link SWTBotButton}.
 */
public class ButtonAssertions extends AbstractSWTBotAssertions<ButtonAssertions, SWTBotButton> {

	protected ButtonAssertions(final SWTBotButton actual) {
		super(actual, ButtonAssertions.class);
	}

	public static ButtonAssertions assertThat(final SWTBotButton actual) {
		return new ButtonAssertions(actual);
	}

}
