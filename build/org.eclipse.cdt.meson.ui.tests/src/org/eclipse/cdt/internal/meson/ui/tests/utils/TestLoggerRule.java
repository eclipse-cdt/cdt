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

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 *
 */
public class TestLoggerRule extends TestWatcher {

	@Override
	protected void starting(final Description description) {
		System.out.println("Starting " + description.getClassName() + "." + description.getMethodName());
	}
}
