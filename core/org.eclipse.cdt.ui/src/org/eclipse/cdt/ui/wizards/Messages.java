/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;

import org.eclipse.osgi.util.NLS;

class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.ui.wizards.messages"; //$NON-NLS-1$
	public static String NewCDTProjectWizard_mainPageDesc;
	public static String NewCDTProjectWizard_mainPageTitle;
	public static String NewCDTProjectWizard_refPageDesc;
	public static String NewCDTProjectWizard_refPageTitle;
	public static String NewCDTProjectWizard_templatePageDesc;
	public static String NewCDTProjectWizard_templatePageTitle;
	public static String NewCDTProjectWizard_windowTitle;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
