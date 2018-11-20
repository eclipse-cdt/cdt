/*******************************************************************************
 * Copyright (c) 2007, 2012 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     Broadcom Corporation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @since 5.2
 */
public class ExpIncludeFileTab extends AbstractExportTab {

	@Override
	public ICLanguageSettingEntry doAdd(String s1, String s2, boolean isWsp) {
		int flags = isWsp ? ICSettingEntry.VALUE_WORKSPACE_PATH : 0;
		return CDataUtil.createCIncludeFileEntry(s2, flags);
	}

	@Override
	public ICLanguageSettingEntry doEdit(String s1, String s2, boolean isWsp) {
		return doAdd(s1, s2, isWsp);
	}

	@Override
	public int getKind() {
		return ICSettingEntry.INCLUDE_FILE;
	}

	@Override
	public boolean canBeVisible() {
		if (!CDTPrefUtil.getBool(CDTPrefUtil.KEY_SHOW_INC_FILES))
			return false;
		return super.canBeVisible();
	}

	@Override
	public boolean hasValues() {
		return false;
	}
}
