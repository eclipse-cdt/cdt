/*******************************************************************************
 * Copyright (c) 2019 Marc-Andre Laperle
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc-Andre Laperle - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.msw.build.tests;

import org.junit.Test;

/**
 * A dummy tests so that a test is found when running with Maven/Tycho on
 * non-Windows platforms, instead of having to do heavy pom.xml gymnastics
 * (pomless builds, etc).
 */
public class DummyTest {
	@Test
	public void testOneEntry() throws Exception {

	}
}
