/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.preferences;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.ui.properties.Messages;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 5.1
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class WizardDefaultsTab extends AbstractCPropertyTab {

    private Button show_sup;
    private Button show_oth;

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(1, false));

		show_sup = new Button(usercomp, SWT.CHECK);
        show_sup.setText(Messages.getString("WizardDefaultsTab.0")); //$NON-NLS-1$
        show_sup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		show_oth = new Button(usercomp, SWT.CHECK);
        show_oth.setText(Messages.getString("WizardDefaultsTab.1")); //$NON-NLS-1$
        show_oth.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		show_sup.setSelection(!CDTPrefUtil.getBool(CDTPrefUtil.KEY_NOSUPP));
		show_oth.setSelection(CDTPrefUtil.getBool(CDTPrefUtil.KEY_OTHERS));
	}

	@Override
	protected void performOK() {
		CDTPrefUtil.setBool(CDTPrefUtil.KEY_NOSUPP, !show_sup.getSelection());
		CDTPrefUtil.setBool(CDTPrefUtil.KEY_OTHERS, show_oth.getSelection());
	}
	
	@Override
	protected void performDefaults() {
		show_sup.setSelection(true);
		show_oth.setSelection(false);
	}

	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) { performOK(); }
	@Override
	protected void updateData(ICResourceDescription cfg) {} // Do nothing. Data is read once after creation
	@Override
	protected void updateButtons() {} // Do nothing. No buttons to update.
}
