/*******************************************************************************
 * Copyright (c) 2011 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Emanuel Graf - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.togglefunction;

import org.eclipse.cdt.internal.ui.refactoring.togglefunction.ToggleNodeHelper;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * @author egraf
 */
public class ToggleNodeHelperTest extends TestCase {
	/**
	 * Test method for {@link org.eclipse.cdt.internal.ui.refactoring.togglefunction.ToggleNodeHelper#getFilenameWithoutExtension(java.lang.String)}.
	 */
	@Test
	public void testGetFilenameWithoutExtension1() {
		assertEquals("MyClass", ToggleNodeHelper.getFilenameWithoutExtension("MyClass.h"));
	}

	/**
	 * Test method for {@link org.eclipse.cdt.internal.ui.refactoring.togglefunction.ToggleNodeHelper#getFilenameWithoutExtension(java.lang.String)}.
	 */
	@Test
	public void testGetFilenameWithoutExtension2() {
		assertEquals("My.Class", ToggleNodeHelper.getFilenameWithoutExtension("My.Class.h"));
	}
}
