/*******************************************************************************
 * Copyright (c) 2004, 2008 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards;

import org.eclipse.jface.action.IAction;

public class NewFileDropDownAction extends AbstractWizardDropDownAction {

	public NewFileDropDownAction() {
		super();
		//		WorkbenchHelp.setHelp(this, ICHelpContextIds.OPEN_FILE_WIZARD_ACTION);
	}

	@Override
	protected IAction[] getWizardActions() {
		return CWizardRegistry.getFileWizardActions();
	}
}
