/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
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
package org.eclipse.cdt.managedbuilder.xlupc.ui.wizards;

import org.eclipse.cdt.managedbuilder.xlc.ui.wizards.XLCSettingsWizardPage;
import org.eclipse.cdt.managedbuilder.xlupc.ui.Messages;

/**
 *
 */
public class XLUpcSettingsWizardPage extends XLCSettingsWizardPage {

	public static final String PAGE_ID = "org.eclipse.cdt.managedbuilder.xlupc.ui.wizards.XLUpcSettingsWizardPage"; //$NON-NLS-1$

	public XLUpcSettingsWizardPage(String pageID) {
		super(pageID);
	}

	public XLUpcSettingsWizardPage() {
		super();
		pageID = PAGE_ID;
	}

	@Override
	public String getName() {
		return Messages.XLUpcSettingsWizardPage_0;
	}

	@Override
	public String getTitle() {
		return Messages.XLUpcSettingsWizardPage_1;
	}

}
