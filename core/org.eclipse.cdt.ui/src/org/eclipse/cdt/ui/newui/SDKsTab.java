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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;

public class SDKsTab  extends AbstractCPropertyTab {

	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout());
		Label l = new Label(usercomp, SWT.NONE);
		l.setLayoutData(new GridData(GridData.BEGINNING));
	}

	public void updateData(ICResourceDescription cfg) {
	}
	public void performApply(ICResourceDescription src,ICResourceDescription dst) {
	}
	protected void performDefaults() {
	}

	// This page can be displayed for project only
	public boolean canBeVisible() {
		return page.isForProject();
	}

}
