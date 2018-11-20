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
package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock;
import org.eclipse.cdt.ui.wizards.NewCProjectWizardOptionPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @deprecated as of CDT 4.0. Does not seem to be used anywhere, looks like
 *    remnant of 3.X style new project wizard.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@Deprecated
public class CNewOptionsPage extends NewCProjectWizardOptionPage {

	public CNewOptionsPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	public CNewOptionsPage(String pageName) {
		this(pageName, null, null);
	}

	@Override
	protected TabFolderOptionBlock createOptionBlock() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Preferences getPreferences() {
		return null;
	}

	@Override
	public IProject getProject() {
		return null;
	}

}
