/*******************************************************************************
 * Copyright (c) 2007, 2011 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.templateengine;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.templateengine.process.ProcessHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;

public class TestTemplateEngineBugs extends BaseTestCase {

	public void testBug215283() {
		assertEquals(set("a"), ProcessHelper.getReplaceKeys("$(a)"));
		assertEquals(set("a", "b"), ProcessHelper.getReplaceKeys("$(a)$(b)"));
		assertEquals(set("a", "b", "cc", "ddd"), ProcessHelper.getReplaceKeys("$(a)$(b)$(cc)$(ddd)"));
		assertEquals(set("aaa", "b", "c", "dd"), ProcessHelper.getReplaceKeys("$(aaa)$(b)$(c)$(dd)"));
		assertEquals(set("a"), ProcessHelper.getReplaceKeys("($(a))"));
		assertEquals(set("a", "b"), ProcessHelper.getReplaceKeys("$(b)$(a)"));
		assertEquals(set("a"), ProcessHelper.getReplaceKeys("        \n$(a)"));
		assertEquals(set("a"), ProcessHelper.getReplaceKeys("$(a)          "));
	}

	private Set<String> set(String... s) {
		HashSet<String> result = new HashSet<>();
		result.addAll(Arrays.asList(s));
		return result;
	}
}
