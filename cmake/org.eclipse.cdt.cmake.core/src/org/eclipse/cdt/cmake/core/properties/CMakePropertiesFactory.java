/*******************************************************************************
 * Copyright (c) 2025 Renesas Electronics Europe.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.properties;

import org.eclipse.cdt.cmake.core.internal.properties.CMakePropertiesBean;

/**
 * @since 2.0
 */
public class CMakePropertiesFactory {
	public static ICMakeProperties createProperties() {
		return new CMakePropertiesBean();
	}
}
