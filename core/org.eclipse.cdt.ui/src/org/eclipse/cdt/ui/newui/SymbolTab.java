/*******************************************************************************
 * Copyright (c) 2007, 2013 Intel Corporation and others.
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
 *     IBM Corporation
 *     Marc-Andre Laperle (Ericsson) - Adjust the columns width
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import java.util.Collections;

import org.eclipse.cdt.core.model.util.CDTListComparator;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class SymbolTab extends AbstractLangsListTab {
	private TableColumn symbolColumn;
	private TableColumn valueColumn;
	private ControlAdapter listener;

	@Override
	public void additionalTableSet() {
		symbolColumn = new TableColumn(table, SWT.LEFT);
		symbolColumn.setText(Messages.SymbolTab_0);
		symbolColumn.setToolTipText(Messages.SymbolTab_0);
		valueColumn = new TableColumn(table, SWT.LEFT);
		valueColumn.setText(Messages.SymbolTab_1);
		valueColumn.setToolTipText(Messages.SymbolTab_1);
		table.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				e.result = Messages.SymbolTab_0;
			}
		});
	}

	@Override
	public ICLanguageSettingEntry doAdd() {
		SymbolDialog dlg = new SymbolDialog(usercomp.getShell(), true, Messages.SymbolTab_2, EMPTY_STR, EMPTY_STR,
				getResDesc());
		if (dlg.open() && dlg.text1.trim().length() > 0) {
			toAllCfgs = dlg.check1;
			toAllLang = dlg.check3;
			return CDataUtil.createCMacroEntry(dlg.text1, dlg.text2, 0);
		}
		return null;
	}

	@Override
	public ICLanguageSettingEntry doEdit(ICLanguageSettingEntry ent) {
		SymbolDialog dlg = new SymbolDialog(usercomp.getShell(), false, Messages.SymbolTab_3, ent.getName(),
				ent.getValue(), getResDesc());
		if (dlg.open())
			return CDataUtil.createCMacroEntry(dlg.text1, dlg.text2, 0);
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
		updateStringListModeControl();
		updateStatusLine();
		updateButtons();
	}

	private void adjustColumnsWidth() {
		// Do the adjustment only once
		table.removeControlListener(listener);

		symbolColumn.pack();
		valueColumn.pack();

		int symbolColumnWidth = symbolColumn.getWidth();
		int valueColumnWidth = valueColumn.getWidth();
		int tableWidth = table.getClientArea().width;
		int excessWidth = tableWidth - symbolColumnWidth - valueColumnWidth;
		if (excessWidth > 0) {
			// Distribute excess width evenly
			symbolColumn.setWidth(symbolColumnWidth + excessWidth / 2);
			valueColumn.setWidth(valueColumnWidth + excessWidth / 2);
		} else if (excessWidth < 0) {
			// Content too large, split in two
			symbolColumn.setWidth(tableWidth / 2);
			valueColumn.setWidth(tableWidth / 2);
		}
	}

	@Override
	public void createControls(final Composite parent) {
		super.createControls(parent);
		showBIButton.setSelection(true);
		ImportExportWizardButtons.addWizardLaunchButtons(usercomp, page.getElement());

		listener = new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				adjustColumnsWidth();
			}
		};
		table.addControlListener(listener);
	}
}
