/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.ui.wizards;

import org.eclipse.tools.templates.ui.TemplateSelectionPage;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

public class NewQtProjectWizard extends BasicNewResourceWizard {

	private static final String QT_TAG_ID = "org.eclipse.cdt.qt.ui.tag"; //$NON-NLS-1$

	private TemplateSelectionPage templateSelectionPage;

	@Override
	public void addPages() {
		templateSelectionPage = new TemplateSelectionPage("templateSelection", QT_TAG_ID); //$NON-NLS-1$
		templateSelectionPage.setTitle("Template for New Qt Project");
		this.addPage(templateSelectionPage);
	}

	@Override
	public boolean performFinish() {
		return true;
	}

}
