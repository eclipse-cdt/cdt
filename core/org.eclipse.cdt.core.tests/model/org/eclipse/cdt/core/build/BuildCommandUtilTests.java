/*******************************************************************************
 * Copyright (c) 2026 Harsh Bhanushali and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.cdt.internal.core.build.BuildCommandUtil;
import org.junit.jupiter.api.Test;

public class BuildCommandUtilTests {

	@Test
	public void makeStyleCommandsAcceptMakefileOption() {
		assertTrue(BuildCommandUtil.acceptsMakefileOption("make"));
		assertTrue(BuildCommandUtil.acceptsMakefileOption("gmake"));
		assertTrue(BuildCommandUtil.acceptsMakefileOption("nmake"));
		assertTrue(BuildCommandUtil.acceptsMakefileOption("mingw32-make"));
		assertTrue(BuildCommandUtil.acceptsMakefileOption("make.exe"));
		assertTrue(BuildCommandUtil.acceptsMakefileOption("/usr/bin/make"));
		assertTrue(BuildCommandUtil.acceptsMakefileOption("C:\\tools\\MinGW\\bin\\mingw32-make.exe"));
	}

	@Test
	public void otherBuildToolsDoNotAcceptMakefileOption() {
		assertFalse(BuildCommandUtil.acceptsMakefileOption("ninja"));
		assertFalse(BuildCommandUtil.acceptsMakefileOption("/usr/bin/ninja"));
		assertFalse(BuildCommandUtil.acceptsMakefileOption("ninja.exe"));
		assertFalse(BuildCommandUtil.acceptsMakefileOption("cmake"));
		assertFalse(BuildCommandUtil.acceptsMakefileOption("meson"));
		assertFalse(BuildCommandUtil.acceptsMakefileOption("./build.sh"));
		assertFalse(BuildCommandUtil.acceptsMakefileOption(""));
		assertFalse(BuildCommandUtil.acceptsMakefileOption(null));
	}
}
