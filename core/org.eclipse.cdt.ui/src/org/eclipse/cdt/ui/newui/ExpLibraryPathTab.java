/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.cdt.core.resources.IPathEntryStoreListener;
import org.eclipse.cdt.core.resources.PathEntryStoreChangedEvent;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ExpLibraryPathTab extends AbstractExportTab implements IPathEntryStoreListener {

	@Override
	public void pathEntryStoreChanged(PathEntryStoreChangedEvent event) {
		updateData(getResDesc());
	}

	@Override
	public ICLanguageSettingEntry doAdd(String s1, String s2, boolean isWsp) {
		int flags = isWsp ? ICSettingEntry.VALUE_WORKSPACE_PATH : 0;
		return new CLibraryPathEntry(s2, flags);
	}

	@Override
	public ICLanguageSettingEntry doEdit(String s1, String s2, boolean isWsp) {
		return doAdd(s1, s2, isWsp);
	}

	@Override
	public int getKind() {
		return ICSettingEntry.LIBRARY_PATH;
	}

	@Override
	public boolean hasValues() {
		return false;
	}
}
