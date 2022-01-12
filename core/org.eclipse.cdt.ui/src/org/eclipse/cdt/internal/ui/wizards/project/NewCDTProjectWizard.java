/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.project;

import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.tools.templates.ui.NewWizard;

public class NewCDTProjectWizard extends NewWizard {

	private static final String cdtTag = "org.eclipse.cdt.ui.cdtTag"; //$NON-NLS-1$

	public NewCDTProjectWizard() {
		super(cdtTag);
		setWindowTitle(CUIMessages.NewCDTProjectWizard_Title);
		setTemplateSelectionPageTitle(CUIMessages.NewCDTProjectWizard_PageTitle);
	}

}
