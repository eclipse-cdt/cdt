/*******************************************************************************
 * Copyright (c) 2020 Marc-Andre Laperle.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.ui.wizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.managedbuilder.internal.ui.wizards.messages"; //$NON-NLS-1$
	public static String WizardMakeProjectConversion_title;
	public static String WizardMakeProjectConversion_description;
	public static String WizardMakeProjectConversion_monitor_convertingToMakeProject;
	public static String WizardMakeProjectConversion_projectOptions_title;
	public static String WizardMakeProjectConversion_projectOptions_projectType;
	public static String WizardMakeProjectConversion_projectOptions_projectTypeTable;
	public static String WizardMakeProjectConversion_projectOptions_showSuppressed;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
