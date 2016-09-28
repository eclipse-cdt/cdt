/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal.project;

import org.eclipse.tools.templates.ui.NewWizard;

public class NewArduinoProjectWizard extends NewWizard {

	private static final String ARDUINO_TAG_ID = "org.eclipse.cdt.arduino.ui.tag"; //$NON-NLS-1$

	public NewArduinoProjectWizard() {
		super(ARDUINO_TAG_ID);
		setWindowTitle("New Arduino C++ Project");
		setTemplateSelectionPageTitle("Templates for New Arduino C++ Project");
	}

}
