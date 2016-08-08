/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.ui.wizards;

import org.eclipse.tools.templates.ui.NewWizard;

public class NewQtProjectWizard extends NewWizard {

	private static final String QT_TAG_ID = "org.eclipse.cdt.qt.ui.tag"; //$NON-NLS-1$

	public NewQtProjectWizard() {
		super(QT_TAG_ID);
	}
	
}
