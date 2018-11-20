/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
