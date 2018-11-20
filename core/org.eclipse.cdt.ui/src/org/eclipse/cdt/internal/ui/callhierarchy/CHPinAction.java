/*******************************************************************************
 * Copyright (c) 2011, 2013 IBM Corporation and others.
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
 *     Marc-Andre Laperle - Adapted to CDT from JDT
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.callhierarchy;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

public class CHPinAction extends Action {
	private CHViewPart fView;

	/**
	 * Constructs a 'Pin Call Hierarchy view' action.
	 *
	 * @param view the Call Hierarchy view
	 */
	public CHPinAction(CHViewPart view) {
		super(CHMessages.CHPinAction_label, IAction.AS_CHECK_BOX);
		setToolTipText(CHMessages.CHPinAction_tooltip);
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, "pin_view.gif"); //$NON-NLS-1$
		fView = view;
	}

	@Override
	public void run() {
		fView.setPinned(isChecked());
	}
}
