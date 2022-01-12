/*******************************************************************************
 * Copyright (c) 2001, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Rational Software - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.cdt.ui.actions.MemberFilterActionGroup;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

/**
 * Action used to enable / disable method filter properties
 */

public class MemberFilterAction extends Action {

	private int fFilterProperty;
	private MemberFilterActionGroup fFilterActionGroup;

	public MemberFilterAction(MemberFilterActionGroup actionGroup, String title, int property, String contextHelpId,
			boolean initValue) {
		super(title);
		fFilterActionGroup = actionGroup;
		fFilterProperty = property;

		if (contextHelpId != null) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, contextHelpId);
		}
		setChecked(initValue);
	}

	/**
	 * Returns this action's filter property.
	 */
	public int getFilterProperty() {
		return fFilterProperty;
	}

	/*
	 * @see Action#actionPerformed
	 */
	@Override
	public void run() {
		fFilterActionGroup.setMemberFilter(fFilterProperty, isChecked());
	}

}
