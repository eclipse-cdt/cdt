/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.wizards;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.ui.wizards.conversion.ConversionWizard;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ConvertToMakeWizard extends ConversionWizard {

	@Override
	public void addPages() {
		addPage(mainPage = new ConvertToMakeWizardPage(getPrefix()));
	}

	@Override
	public String getProjectID() {
		return MakeCorePlugin.MAKE_PROJECT_ID;
	}

	@Override
	public String getBuildSystemId() {
		if (!((ConvertToMakeWizardPage) mainPage).isSetProjectType()) {
			return ManagedBuildManager.CFG_DATA_PROVIDER_ID;
		}

		return null;
	}
}
