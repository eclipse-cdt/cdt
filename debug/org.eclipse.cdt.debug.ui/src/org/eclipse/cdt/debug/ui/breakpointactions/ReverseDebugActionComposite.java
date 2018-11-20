/*******************************************************************************
 * Copyright (c) 2007, 2012 Nokia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.breakpointactions;

import org.eclipse.cdt.debug.ui.breakpointactions.ReverseDebugAction.REVERSE_DEBUG_ACTIONS_ENUM;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @since 7.3
 */
public class ReverseDebugActionComposite extends Composite {
	private Combo combo;

	public ReverseDebugActionComposite(Composite parent, int style, ReverseDebugActionPage page) {
		super(parent, style);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		setLayout(gridLayout);

		final Label reverseDebugActionLabel = new Label(this, SWT.NONE);
		reverseDebugActionLabel.setText(Messages.getString("ReverseDebugActionComposite.label")); //$NON-NLS-1$

		// combo widget that lets the user select which reverse debug action to set
		combo = new Combo(this, SWT.READ_ONLY);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// add the available reverse debug actions to the combo drop-down list
		for (REVERSE_DEBUG_ACTIONS_ENUM elem : REVERSE_DEBUG_ACTIONS_ENUM.values()) {
			String option = elem.toString().toLowerCase();
			combo.add(Messages.getString("ReverseDebugAction." + option)); //$NON-NLS-1$
		}
		combo.select(0);
	}

	/**
	 * @return The currently selected reverse debug action
	 */
	public REVERSE_DEBUG_ACTIONS_ENUM getOperation() {
		int index = combo.getSelectionIndex();

		return REVERSE_DEBUG_ACTIONS_ENUM.getValue(index);
	}

}
