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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;

public class IncludeTab extends AbstractLangsListTab {
	
   public void additionalTableSet() {
	   TableColumn c = new TableColumn(table, SWT.NONE);
	   c.setWidth(210);
	   c.setText(UIMessages.getString("IncludeTab.0")); //$NON-NLS-1$
	   showBIButton.setSelection(true);
   }
	
	public ICLanguageSettingEntry doAdd() {
		IncludeDialog dlg = new IncludeDialog(
				usercomp.getShell(), IncludeDialog.NEW_DIR,
				UIMessages.getString("IncludeTab.1"),  //$NON-NLS-1$
				EMPTY_STR, getResDesc().getConfiguration(), 0);
		if (dlg.open() && dlg.text1.trim().length() > 0 ) {
			toAllCfgs = dlg.check1;
			toAllLang = dlg.check3;
			int flags = 0;
			if (dlg.check2) { // isWsp
				flags = ICSettingEntry.VALUE_WORKSPACE_PATH;
			}
			return new CIncludePathEntry(dlg.text1, flags);
		} else 
			return null;
	}

	public ICLanguageSettingEntry doEdit(ICLanguageSettingEntry ent) {
		IncludeDialog dlg = new IncludeDialog(
				usercomp.getShell(), IncludeDialog.OLD_DIR,
				UIMessages.getString("IncludeTab.2"),  //$NON-NLS-1$
				ent.getValue(), getResDesc().getConfiguration(),
				(ent.getFlags() & ICSettingEntry.VALUE_WORKSPACE_PATH));
		if (dlg.open()) {
			int flags = 0;
			if (dlg.check2) flags = ICSettingEntry.VALUE_WORKSPACE_PATH;
			return new CIncludePathEntry(dlg.text1, flags);
		} else 
			return null;
	}
	
	public int getKind() { return ICSettingEntry.INCLUDE_PATH; }
}
