/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import java.util.Collections;

import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.cdt.core.model.util.CDTListComparator;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class SymbolTab extends AbstractLangsListTab {
    @Override
	public void additionalTableSet() {
    	TableColumn tc = new TableColumn(table, SWT.LEFT);
    	tc.setText(UIMessages.getString("SymbolTab.0")); //$NON-NLS-1$
    	tc.setWidth(80);
    	tc.setToolTipText(UIMessages.getString("SymbolTab.0")); //$NON-NLS-1$
    	tc = new TableColumn(table, SWT.LEFT);
    	tc.setText(UIMessages.getString("SymbolTab.1")); //$NON-NLS-1$
    	tc.setWidth(130);
    	tc.setToolTipText(UIMessages.getString("SymbolTab.1")); //$NON-NLS-1$
    	table.getAccessible().addAccessibleListener(
				new AccessibleAdapter() {                       
                    @Override
					public void getName(AccessibleEvent e) {
                            e.result = UIMessages.getString("SymbolTab.0"); //$NON-NLS-1$
                    }
                }
		  );
    }

	@Override
	public ICLanguageSettingEntry doAdd() {
		SymbolDialog dlg = new SymbolDialog(
				usercomp.getShell(), true,
				UIMessages.getString("SymbolTab.2"), EMPTY_STR, EMPTY_STR, getResDesc()); //$NON-NLS-1$
		if (dlg.open() && dlg.text1.trim().length() > 0 ) {
			toAllCfgs = dlg.check1;
			toAllLang = dlg.check3;
			return new CMacroEntry(dlg.text1, dlg.text2, 0);
		}
		return null;
	}

	@Override
	public ICLanguageSettingEntry doEdit(ICLanguageSettingEntry ent) {
		SymbolDialog dlg = new SymbolDialog(
				usercomp.getShell(), false,
				UIMessages.getString("SymbolTab.3"), ent.getName(),  //$NON-NLS-1$
				ent.getValue(), getResDesc());
		if (dlg.open())
			return new CMacroEntry(dlg.text1, dlg.text2, 0);
		return null;
	}
	
	@Override
	public int getKind() { 
		return ICSettingEntry.MACRO; 
	}

	// Specific version of "update()" for Symbols tab only
	@Override
	public void update() {
		if (lang != null) {
			int x = table.getSelectionIndex();
			if (x == -1) 
				x = 0;
			shownEntries = getIncs();
			Collections.sort(shownEntries, CDTListComparator.getInstance());
			tv.setInput(shownEntries.toArray(new Object[shownEntries.size()]));
			if (table.getItemCount() > x)
				table.setSelection(x);
			else if (table.getItemCount() > 0) 
				table.setSelection(0);
		}		
		updateLbs(lb1, lb2);
		updateButtons();
	}
	
	
	@Override
	public void createControls(final Composite parent) {
		super.createControls(parent);
		showBIButton.setSelection(true);
		ImportExportWizardButtons.addWizardLaunchButtons(usercomp, page.getElement());
	}
}
