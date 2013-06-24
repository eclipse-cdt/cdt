/*******************************************************************************
 * Copyright (c) 2013, 2013 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.tests;

import static org.junit.Assert.fail;

import org.eclipse.cdt.make.ui.tests.AllMakeUITests.ToDo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ToDo.class })
public class AllMakeUITests {
	public static class ToDo {
		@Test
		public void test() {
			fail("Not yet implemented");
		}
	}
}
