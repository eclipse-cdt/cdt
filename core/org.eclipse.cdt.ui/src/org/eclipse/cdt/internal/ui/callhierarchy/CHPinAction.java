/*******************************************************************************
 * Copyright (c) 2011, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Marc-Andre Laperle - Adapted to CDT from JDT
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.callhierarchy;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import org.eclipse.cdt.internal.ui.CPluginImages;

public class CHPinAction extends Action {
	private CHViewPart fView= null;

	/**
	 * Constructs a 'Pin Call Hierarchy view' action.
	 * 
	 * @param view the Call Hierarchy view
	 */
	public CHPinAction(CHViewPart view) {
		super(CHMessages.CHPinAction_label, IAction.AS_CHECK_BOX);
		setToolTipText(CHMessages.CHPinAction_tooltip);
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, "pin_view.gif"); //$NON-NLS-1$
		fView= view;
	}

	/*
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		fView.setPinned(isChecked());
	}
}
