/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.newui;

import org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock;
import org.eclipse.cdt.ui.wizards.NewCProjectWizardOptionPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.resource.ImageDescriptor;

public class CNewOptionsPage extends NewCProjectWizardOptionPage {

	public CNewOptionsPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
	public CNewOptionsPage(String pageName) {
		this(pageName, null, null);
	}

	protected TabFolderOptionBlock createOptionBlock() {
		// TODO Auto-generated method stub
		return null;
	}

	public Preferences getPreferences() {
		return null;
	}

	public IProject getProject() {
		return null;
	}

}
