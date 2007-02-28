/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.cdt.core.resources.IPathEntryStore;
import org.eclipse.cdt.core.resources.IPathEntryStoreListener;
import org.eclipse.cdt.core.resources.PathEntryStoreChangedEvent;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;

public class ExpLibraryPathTab extends AbstractExportTab implements IPathEntryStoreListener {
	IPathEntryStore fStore;

	public void pathEntryStoreChanged(PathEntryStoreChangedEvent event) {
		updateData(getResDesc());
	}

	public ICLanguageSettingEntry doAdd(String s1, String s2, boolean isWsp) {
		int flags = isWsp ? ICSettingEntry.VALUE_WORKSPACE_PATH : 0;
		return new CLibraryPathEntry(s2, flags);
	}

	public ICLanguageSettingEntry doEdit(String s1, String s2, boolean isWsp) {
		return doAdd(s1, s2, isWsp);
	}
	
	public int getKind() { return ICSettingEntry.LIBRARY_PATH; }
	public boolean hasValues() { return false; }
}
