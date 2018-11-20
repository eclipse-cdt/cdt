/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

public interface ICSettingBase {
	public static final int SETTING_PROJECT = 1;
	public static final int SETTING_CONFIGURATION = 1 << 1;
	public static final int SETTING_FOLDER = 1 << 2;
	public static final int SETTING_FILE = 1 << 3;
	public static final int SETTING_LANGUAGE = 1 << 4;
	public static final int SETTING_TARGET_PLATFORM = 1 << 5;
	public static final int SETTING_BUILD = 1 << 6;
}
