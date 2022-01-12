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
 *     IBM Corporation
 *     Broadcom Corporation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

/**
 * This class provides UI for the {@link ICSettingEntry#INCLUDE_FILE}
 * option type.
 *<p>
 * This tab is hidden by default and can be shown under: <br/>
 * 		Window > Preferences > C/C++ > Property Page Settings > Show "Include Files" Tab
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @since 5.2
 */
public class IncludeFileTab extends AbstractLangsListTab {

	@Override
	public void additionalTableSet() {
		columnToFit = new TableColumn(table, SWT.NONE);
		columnToFit.setText(Messages.IncludeFileTab_0);
		columnToFit.setToolTipText(Messages.IncludeFileTab_0);
		showBIButton.setSelection(true);
		table.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				e.result = Messages.IncludeFileTab_0;
			}
		});
	}

	@Override
	public ICLanguageSettingEntry doAdd() {
		IncludeDialog dlg = new IncludeDialog(usercomp.getShell(), IncludeDialog.NEW_FILE, Messages.IncludeFileTab_1,
				EMPTY_STR, getResDesc().getConfiguration(), 0, ICSettingEntry.INCLUDE_FILE);
		if (dlg.open() && dlg.text1.trim().length() > 0) {
			toAllCfgs = dlg.check1;
			toAllLang = dlg.check3;
			int flags = 0;
			if (dlg.check2) { // isWsp
				flags = ICSettingEntry.VALUE_WORKSPACE_PATH;
			}
			return CDataUtil.createCIncludeFileEntry(dlg.text1, flags);
		}
		return null;
	}

	@Override
	public ICLanguageSettingEntry doEdit(ICLanguageSettingEntry ent) {
		IncludeDialog dlg = new IncludeDialog(usercomp.getShell(), IncludeDialog.OLD_FILE, Messages.IncludeFileTab_2,
				ent.getValue(), getResDesc().getConfiguration(), ent.getFlags() & ICSettingEntry.VALUE_WORKSPACE_PATH,
				ICSettingEntry.INCLUDE_FILE);
		if (dlg.open()) {
			int flags = 0;
			if (dlg.check2)
				flags = ICSettingEntry.VALUE_WORKSPACE_PATH;
			return CDataUtil.createCIncludeFileEntry(dlg.text1, flags);
		}
		return null;
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
	public void createControls(final Composite parent) {
		super.createControls(parent);
		ImportExportWizardButtons.addWizardLaunchButtons(usercomp, page.getElement());
	}
}
