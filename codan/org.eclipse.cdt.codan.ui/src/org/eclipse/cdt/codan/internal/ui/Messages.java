/*******************************************************************************
 * Copyright (c) 2009,2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui;

import org.eclipse.osgi.util.NLS;

/**
 * TODO: add description
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.codan.internal.ui.messages"; //$NON-NLS-1$
	public static String BuildPropertyPage_RunAsYouType;
	public static String BuildPropertyPage_RunWithBuild;
	public static String CheckedTreeEditor_SelectionCannotBeEmpty;
	public static String CodanPreferencePage_Customize;
	public static String CodanPreferencePage_HasParameters;
	public static String CodanPreferencePage_Info;
	public static String CodanPreferencePage_NoInfo;
	public static String CodanPreferencePage_NoParameters;
	public static String ProblemsTreeEditor_NameColumn;
	public static String ProblemsTreeEditor_Problems;
	public static String ProblemsTreeEditor_SeverityColumn;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
