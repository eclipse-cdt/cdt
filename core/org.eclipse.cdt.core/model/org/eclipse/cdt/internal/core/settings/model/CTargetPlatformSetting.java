/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
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
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.settings.model.ICTargetPlatformSetting;
import org.eclipse.cdt.core.settings.model.extension.CTargetPlatformData;

public class CTargetPlatformSetting extends CDataProxy implements ICTargetPlatformSetting {

	CTargetPlatformSetting(CTargetPlatformData data, CConfigurationDescription cfg) {
		super(data, cfg, cfg);
	}

	@Override
	public final int getType() {
		return SETTING_TARGET_PLATFORM;
	}

	@Override
	public String[] getBinaryParserIds() {
		CTargetPlatformData data = getTargetPlatformData(false);
		return data.getBinaryParserIds();
	}

	@Override
	public void setBinaryParserIds(String[] ids) {
		CTargetPlatformData data = getTargetPlatformData(true);
		data.setBinaryParserIds(ids);
	}

	private CTargetPlatformData getTargetPlatformData(boolean write) {
		return (CTargetPlatformData) getData(write);
	}
}
