/*******************************************************************************
 * Copyright (c) 2025 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class WarnAboutJdtUiBug {

	/**
	 * This test is a simple way to increase the chance that devs read the documentation.
	 */
	@Test
	@Tag("jdt-ui-bug")
	public void warnByFailingATest() {
		fail("""
				JDT Has a bug when running JUnit5 that it doesn't see inner classes as tests like
				org.eclipse.cdt.internal.index.tests.IndexCBindingResolutionBugsTest.SingleProjectTest.
				This means if you run parser tests within Eclipse a lot of tests will not run at all,
				giving a false sense of security. Therefore we have this test that always fails (unless
				excluded) to make sure devs don't accidentally miss this.

				See TESTING.md section "JDT UI test discovery" for more information.
				""");

	}
}
