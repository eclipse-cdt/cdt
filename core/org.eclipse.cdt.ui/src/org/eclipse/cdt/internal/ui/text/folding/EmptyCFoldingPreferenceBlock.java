/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.folding;

import org.eclipse.cdt.ui.text.folding.ICFoldingPreferenceBlock;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Empty preference block for extensions to the
 * <code>org.eclipse.cdt.ui.foldingStructureProviders</code> extension
 * point that do not specify their own.
 *
 * @since 3.0
 */
public class EmptyCFoldingPreferenceBlock implements ICFoldingPreferenceBlock {
	/*
	 * @see org.eclipse.cdt.internal.ui.text.folding.ICFoldingPreferences#createControl(org.eclipse.swt.widgets.Group)
	 */
	@Override
	public Control createControl(Composite composite) {
		Composite inner = new Composite(composite, SWT.NONE);
		inner.setLayout(new GridLayout(3, false));

		Label label = new Label(inner, SWT.CENTER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 30;
		label.setLayoutData(gd);

		label = new Label(inner, SWT.CENTER);
		label.setText(FoldingMessages.EmptyCFoldingPreferenceBlock_emptyCaption);
		gd = new GridData(GridData.CENTER);
		label.setLayoutData(gd);

		label = new Label(inner, SWT.CENTER);
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 30;
		label.setLayoutData(gd);

		return inner;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.folding.ICFoldingPreferenceBlock#initialize()
	 */
	@Override
	public void initialize() {
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.folding.ICFoldingPreferenceBlock#performOk()
	 */
	@Override
	public void performOk() {
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.folding.ICFoldingPreferenceBlock#performDefaults()
	 */
	@Override
	public void performDefaults() {
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.folding.ICFoldingPreferenceBlock#dispose()
	 */
	@Override
	public void dispose() {
	}

}
