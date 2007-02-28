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

import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;

public class ExpSymbolTab extends AbstractExportTab {

	// isWsp is ignored for symbols
	public ICLanguageSettingEntry doAdd(String s1, String s2, boolean isWsp) {
		return new CMacroEntry(s1, s2, 0);
	}

	public ICLanguageSettingEntry doEdit(String s1, String s2, boolean isWsp) {
		return doAdd(s1, s2, isWsp);
	}
	
	public int getKind() { return ICSettingEntry.MACRO; }
	public boolean hasValues() { return true; }
}
