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

public class ExpSymbolTab extends AbstractExportTab {

	public ICLanguageSettingEntry doAdd(String s1, String s2) {
		return new CMacroEntry(s1, s2, 0);
	}

	public ICLanguageSettingEntry doEdit(String s1, String s2) {
		return doAdd(s1, s2);
	}
	
	public int getKind() { return ICLanguageSettingEntry.MACRO; }
	public boolean hasValues() { return true; }
}
