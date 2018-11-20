/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.internal.tests;

import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class StringBuilderTest extends TestCase {
	public static Test suite() {
		return new TestSuite(StringBuilderTest.class);
	}

	public void testSafe() {
		StringBuilder b1 = new StringBuilder();
		StringBuilder b2 = new StringBuilder();
		b1.append("a");
		b2.append("b");
		CharSequence cs = b2;
		b1.append(cs);
		assertEquals("ab", b1.toString());
	}

	public void testBug220158() {
		StringBuilder b1 = new StringBuilder();
		StringBuilder b2 = new StringBuilder();
		b1.append("a");
		b2.append("b");
		b1.append(b2);
		assertEquals("ab", b1.toString());
	}

	public void testStringBuilderMethods() throws Exception {
		Class clazz = StringBuilder.class;
		Method method = clazz.getMethod("append", CharSequence.class);
		assertNotNull(method);
		try {
			method = clazz.getMethod("append", StringBuilder.class);
			fail();
		} catch (NoSuchMethodException m) {
			// ok
		}
	}
}
