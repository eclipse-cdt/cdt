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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.settings.model.SettingsModelMessages;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public final class CConfigurationStatus extends Status {
	//	public static final int WRONG_TARGET_ARCH = 1;
	//	public static final int WRONG_TARGET_OS = 1 << 1;
	public static final int TOOLCHAIN_NOT_SUPPORTED = 1 << 2;
	public static final int SETTINGS_INVALID = 1 << 3;

	public static final CConfigurationStatus CFG_STATUS_OK = new CConfigurationStatus(0, ""); //$NON-NLS-1$

	public static final CConfigurationStatus CFG_STATUS_ERROR = new CConfigurationStatus(SETTINGS_INVALID,
			SettingsModelMessages.getString("CConfigurationStatus.1")); //$NON-NLS-1$

	public CConfigurationStatus(String pluginId, int code, String message, Throwable exception) {
		super(calcSeverity(code), pluginId, code, message, exception);
	}

	public CConfigurationStatus(int code, String message, Throwable exception) {
		this(CCorePlugin.PLUGIN_ID, code, message, exception);
	}

	public CConfigurationStatus(int code, String message) {
		this(CCorePlugin.PLUGIN_ID, code, message, null);
	}

	private static boolean checkFlags(int flags, int value) {
		return (flags & value) == value;
	}

	private static int calcSeverity(int flags) {
		if (checkFlags(flags, SETTINGS_INVALID))
			return IStatus.ERROR;
		else if (flags != 0)
			return IStatus.WARNING;
		return IStatus.OK;
	}

}
