/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.core.internal.properties;

import org.eclipse.cdt.cmake.core.properties.CMakeGenerator;

/**
 * Preferences that override/augment the generic properties when running under
 * Windows.
 *
 * @author Martin Weber
 */
public class WindowsOverrides extends AbstractOsOverrides {

	/** Overridden to set a sensible generator. */
	@Override
	public void reset() {
		super.reset();
		setGenerator(CMakeGenerator.MinGWMakefiles);
	}
}
