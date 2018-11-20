/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
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
package org.eclipse.cdt.core.settings.model.extension;

import org.eclipse.cdt.core.settings.model.ICSettingBase;

public abstract class CTargetPlatformData extends CDataObject {
	@Override
	public final int getType() {
		return ICSettingBase.SETTING_TARGET_PLATFORM;
	}

	public abstract String[] getBinaryParserIds();

	public abstract void setBinaryParserIds(String[] ids);
}
