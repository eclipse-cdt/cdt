/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.internal.tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.cdt.core.options.BaseOption;
import org.junit.Test;

public class BaseOptionTest {

	@Test
	public void testBaseOption() {
		BaseOption<String> option = new BaseOption<>(String.class, "identifier", "default", "name", "description");
		assertEquals("identifier", option.identifer());
		assertEquals("default", option.defaultValue());
		assertEquals("name", option.name());
		assertEquals("description", option.description());
		assertEquals(String.class, option.valueClass());
	}

	@Test(expected = NullPointerException.class)
	public void testBaseOptionNullValueType() {
		new BaseOption<>(null, "identifier", "default", "name", "description");
	}

	@Test(expected = NullPointerException.class)
	public void testBaseOptionNullIdentifier() {
		new BaseOption<>(Object.class, null, "default", "name", "description");
	}

	@Test(expected = NullPointerException.class)
	public void testBaseOptionNullDefaultValue() {
		new BaseOption<>(Object.class, "identifier", null, "name", "description");
	}

	@Test(expected = NullPointerException.class)
	public void testBaseOptionNullName() {
		new BaseOption<>(Object.class, "identifier", "default", null, "description");
	}

	@Test(expected = NullPointerException.class)
	public void testBaseOptionNullDescription() {
		new BaseOption<>(Object.class, "identifier", "default", "name", null);
	}

}
