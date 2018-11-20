/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.cdt.internal.ui.wizards;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;

public class NewProjectDropDownAction extends AbstractWizardDropDownAction {

	public NewProjectDropDownAction() {
		super();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.OPEN_PROJECT_WIZARD_ACTION);
	}

	@Override
	protected IAction[] getWizardActions() {
		return CWizardRegistry.getProjectWizardActions();
	}
}
