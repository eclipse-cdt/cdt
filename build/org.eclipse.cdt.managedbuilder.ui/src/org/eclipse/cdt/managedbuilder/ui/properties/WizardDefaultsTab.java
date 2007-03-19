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
package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class WizardDefaultsTab extends AbstractCPropertyTab {

    private Button show_sup;
    private Button show_oth;
    private Button show_mng;

	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(1, false));

		show_sup = new Button(usercomp, SWT.CHECK);
        show_sup.setText(Messages.getString("WizardDefaultsTab.0")); //$NON-NLS-1$
        show_sup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		show_oth = new Button(usercomp, SWT.CHECK);
        show_oth.setText(Messages.getString("WizardDefaultsTab.1")); //$NON-NLS-1$
        show_oth.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label l = new Label(usercomp, SWT.WRAP);
        l.setText("\n This checkbox will be moved to another page soon"); //$NON-NLS-1$
        l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        show_mng = new Button(usercomp, SWT.CHECK);
        show_mng.setText(Messages.getString("WizardDefaultsTab.2")); //$NON-NLS-1$
        show_mng.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
}

	protected void performOK() {
		CDTPrefUtil.setBool(CDTPrefUtil.KEY_UNSUPP, !show_sup.getSelection());
		CDTPrefUtil.setBool(CDTPrefUtil.KEY_OTHERS, show_oth.getSelection());
		CDTPrefUtil.setBool(CDTPrefUtil.KEY_MANAGE, show_mng.getSelection());
	}
	
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) { performOK(); }

	protected void performDefaults() {
		show_sup.setSelection(true);
		show_oth.setSelection(false);
		show_mng.setSelection(false);
	}

	protected void updateData(ICResourceDescription cfg) {
		show_sup.setSelection(!CDTPrefUtil.getBool(CDTPrefUtil.KEY_UNSUPP));
		show_oth.setSelection(CDTPrefUtil.getBool(CDTPrefUtil.KEY_OTHERS));
		show_mng.setSelection(CDTPrefUtil.getBool(CDTPrefUtil.KEY_MANAGE));
	}

}
