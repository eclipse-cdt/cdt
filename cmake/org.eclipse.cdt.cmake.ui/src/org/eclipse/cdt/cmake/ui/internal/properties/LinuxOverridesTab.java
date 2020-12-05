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

package org.eclipse.cdt.cmake.ui.internal.properties;

import org.eclipse.cdt.cmake.core.properties.CMakeGenerator;
import org.eclipse.cdt.cmake.core.properties.IOsOverrides;
import org.eclipse.swt.widgets.Composite;

/**
 * UI to control build-platform specific project properties for linux.
 *
 * @author Martin Weber
 */
public class LinuxOverridesTab extends AbstractOverridesTab<IOsOverrides> {

	private static CMakeGenerator[] generators = new CMakeGenerator[] { CMakeGenerator.Ninja,
			CMakeGenerator.UnixMakefiles };

	public LinuxOverridesTab(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected CMakeGenerator[] getAvailableGenerators() {
		return generators;
	}

}
