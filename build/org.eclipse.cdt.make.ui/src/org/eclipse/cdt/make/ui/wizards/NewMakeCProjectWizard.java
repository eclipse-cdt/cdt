/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.wizards;

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;

/**
 * This wizard was used for 3.X style projects. It is left here for compatibility
 * reasons only. The wizard is superseded by MBS C Project Wizard,
 * class {@link org.eclipse.cdt.ui.wizards.CProjectWizard}.
 *
 * @deprecated as of CDT 4.0.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@Deprecated
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

	@Override
	public void addPages() {
		super.addPages();
		addPage(fOptionPage = new MakeProjectWizardOptionPage(MakeUIPlugin.getResourceString(WZ_SETTINGS_TITLE),
				MakeUIPlugin.getResourceString(WZ_SETTINGS_DESC)));
	}

}
