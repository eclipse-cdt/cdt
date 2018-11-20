/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.preferences;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.internal.ui.Messages;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * @since 5.1
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class PropertyMultiCfgTab extends AbstractCPropertyTab {

	private static final int SPACING = 5; // for radio buttons layout

	private Group dGrp;
	private Group wGrp;
	private Button d_1;
	private Button d_2;

	private Button w_0;
	private Button w_1;

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		GridLayout g = new GridLayout(1, false);
		g.verticalSpacing = SPACING;
		usercomp.setLayout(g);

		dGrp = new Group(usercomp, SWT.NONE);
		dGrp.setText(Messages.PropertyMultiCfgTab_3);
		dGrp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		FillLayout fl = new FillLayout(SWT.VERTICAL);
		fl.spacing = SPACING;
		fl.marginHeight = SPACING;
		fl.marginWidth = SPACING;
		dGrp.setLayout(fl);

		Label l = new Label(dGrp, SWT.WRAP);
		l.setText(Messages.PropertyMultiCfgTab_4);

		d_1 = new Button(dGrp, SWT.RADIO);
		d_1.setText(Messages.PropertyMultiCfgTab_6);
		d_2 = new Button(dGrp, SWT.RADIO);
		d_2.setText(Messages.PropertyMultiCfgTab_7);

		wGrp = new Group(usercomp, SWT.NONE);
		wGrp.setText(Messages.PropertyMultiCfgTab_8);
		wGrp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fl = new FillLayout(SWT.VERTICAL);
		fl.spacing = SPACING;
		fl.marginHeight = SPACING;
		fl.marginWidth = SPACING;
		wGrp.setLayout(fl);

		l = new Label(wGrp, SWT.WRAP);
		l.setText(Messages.PropertyMultiCfgTab_9);

		w_0 = new Button(wGrp, SWT.RADIO);
		w_0.setText(Messages.PropertyMultiCfgTab_10);
		w_1 = new Button(wGrp, SWT.RADIO);
		w_1.setText(Messages.PropertyMultiCfgTab_11);

		switch (CDTPrefUtil.getMultiCfgStringListDisplayMode()) {
		case CDTPrefUtil.DMODE_CONJUNCTION:
			d_1.setSelection(true);
			break;
		case CDTPrefUtil.DMODE_DISJUNCTION:
			d_2.setSelection(true);
			break;
		}

		switch (CDTPrefUtil.getMultiCfgStringListWriteMode()) {
		case CDTPrefUtil.WMODE_MODIFY:
			w_0.setSelection(true);
			break;
		case CDTPrefUtil.WMODE_REPLACE:
			w_1.setSelection(true);
			break;
		}
	}

	@Override
	protected void performOK() {
		int x = 0;
		if (d_1.getSelection())
			x = CDTPrefUtil.DMODE_CONJUNCTION;
		else if (d_2.getSelection())
			x = CDTPrefUtil.DMODE_DISJUNCTION;
		CDTPrefUtil.setMultiCfgStringListDisplayMode(x);

		if (w_0.getSelection())
			x = CDTPrefUtil.WMODE_MODIFY;
		else if (w_1.getSelection())
			x = CDTPrefUtil.WMODE_REPLACE;
		CDTPrefUtil.setMultiCfgStringListWriteMode(x);
	}

	@Override
	protected void performDefaults() {
		d_1.setSelection(true);
		d_2.setSelection(false);
		w_0.setSelection(true);
		w_1.setSelection(false);
	}

	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		performOK();
	}

	@Override
	protected void updateData(ICResourceDescription cfg) {
	} // Do nothing. Data is read once after creation

	@Override
	protected void updateButtons() {
	} // Do nothing. No buttons to update
}
