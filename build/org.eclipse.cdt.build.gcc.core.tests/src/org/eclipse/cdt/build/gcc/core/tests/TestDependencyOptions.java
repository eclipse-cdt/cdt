/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.build.gcc.core.tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.build.gcc.core.internal.DependencyOptions;
import org.junit.Test;

/**
 * Tests for {@link DependencyOptions}. See
 * https://github.com/eclipse-cdt/cdt/issues/70
 */
public class TestDependencyOptions {

	private static List<String> remove(String... args) {
		List<String> list = new ArrayList<>(Arrays.asList(args));
		DependencyOptions.remove(list);
		return list;
	}

	@Test
	public void optionsWithoutValue() {
		assertEquals(Arrays.asList("-c", "-g", "-o", "a.o", "a.c"),
				remove("-c", "-g", "-MP", "-MMD", "-o", "a.o", "a.c"));
		assertEquals(Arrays.asList("-c", "a.c"), remove("-M", "-MM", "-MD", "-MG", "-c", "a.c"));
	}

	@Test
	public void optionsWithSeparateValue() {
		assertEquals(Arrays.asList("-c", "-O2", "a.c"),
				remove("-c", "-MD", "-MF", "a.d", "-MT", "a.o", "-MQ", "$(OBJ)", "-MJ", "a.json", "-O2", "a.c"));
	}

	@Test
	public void optionsWithJoinedValue() {
		assertEquals(Arrays.asList("-c", "a.c"), remove("-MFa.d", "-MTa.o", "-c", "a.c"));
	}

	@Test
	public void valueAtEndOfCommandLine() {
		assertEquals(Arrays.asList("-c"), remove("-c", "-MF"));
	}

	@Test
	public void otherOptionsStay() {
		List<String> args = Arrays.asList("-mcpu=cortex-m4", "-DMODE=1", "-I", "inc", "-Wall", "a.c");
		assertEquals(args, remove(args.toArray(new String[0])));
	}
}
