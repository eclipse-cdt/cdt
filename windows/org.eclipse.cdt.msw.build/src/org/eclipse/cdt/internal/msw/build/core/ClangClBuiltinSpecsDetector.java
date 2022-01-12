/*******************************************************************************
 * Copyright (c) 2020 Marc-Andre Laperle.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.msw.build.core;

import org.eclipse.cdt.managedbuilder.language.settings.providers.GCCBuiltinSpecsDetector;

/**
 * Language settings provider to detect built-in compiler settings for the Clang-cl compiler.
 */
public class ClangClBuiltinSpecsDetector extends GCCBuiltinSpecsDetector {

	// This ID is mostly used for the global provider case to get the compiler command from the toolchain
	// which will be wrong here (cl instead of clang-cl), although less wrong than GCC. We would need a dedicated
	// Clang-cl toolchain to make the global case work.
	// But it will correctly display whether or not the associated toolchain (MSVC) is installed and supported, instead of checking GCC.
	private static final String TOOLCHAIN_ID = "org.eclipse.cdt.msvc.toolchain.base"; //$NON-NLS-1$

	@Override
	public String getToolchainId() {
		return TOOLCHAIN_ID;
	}
}
