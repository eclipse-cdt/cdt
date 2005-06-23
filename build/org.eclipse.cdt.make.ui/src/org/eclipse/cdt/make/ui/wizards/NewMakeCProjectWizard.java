/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.wizards;


import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;

/**
 */
public class NewMakeCProjectWizard extends NewMakeProjectWizard {

	private static final String WZ_TITLE = "MakeCWizard.title"; //$NON-NLS-1$
	private static final String WZ_DESC = "MakeCWizard.description"; //$NON-NLS-1$'

	private static final String WZ_SETTINGS_TITLE = "MakeCWizardSettings.title"; //$NON-NLS-1$
	private static final String WZ_SETTINGS_DESC = "MakeCWizardSettings.description"; //$NON-NLS-1$'

	public NewMakeCProjectWizard() {
		this(MakeUIPlugin.getResourceString(WZ_TITLE), MakeUIPlugin.getResourceString(WZ_DESC));
	}

	public NewMakeCProjectWizard(String title, String desc) {
		super(title, desc);
	}

	public void addPages() {
		super.addPages();
		addPage(
			fOptionPage =
				new MakeProjectWizardOptionPage(
					MakeUIPlugin.getResourceString(WZ_SETTINGS_TITLE),
					MakeUIPlugin.getResourceString(WZ_SETTINGS_DESC)));
	}

}
