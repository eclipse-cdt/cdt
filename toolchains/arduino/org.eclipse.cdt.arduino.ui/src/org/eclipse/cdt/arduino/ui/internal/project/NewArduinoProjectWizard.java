/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal.project;

import org.eclipse.tools.templates.ui.TemplateSelectionPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

public class NewArduinoProjectWizard extends BasicNewProjectResourceWizard {

	private static final String ARDUINO_TAG_ID = "org.eclipse.cdt.arduino.ui.tag"; //$NON-NLS-1$

	private TemplateSelectionPage templateSelectionPage;

	public NewArduinoProjectWizard() {
		setForcePreviousAndNextButtons(true);
	}
	
	@Override
	public void addPages() {
		templateSelectionPage = new TemplateSelectionPage("templateSelection", ARDUINO_TAG_ID); //$NON-NLS-1$
		templateSelectionPage.setTitle("Template for New Arduino Project");
		this.addPage(templateSelectionPage);
	}

	@Override
	public boolean performFinish() {
		return true;
	}

}
