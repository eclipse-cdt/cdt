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
 * Externalized messages
 */
public class CodanUIMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.codan.internal.ui.messages"; //$NON-NLS-1$
	public static String BuildPropertyPage_RunAsYouType;
	public static String BuildPropertyPage_RunWithBuild;
	public static String CheckedTreeEditor_SelectionCannotBeEmpty;
	public static String CodanPreferencePage_Customize;
	public static String CodanPreferencePage_HasParameters;
	public static String CodanPreferencePage_Info;
	public static String CodanPreferencePage_MessageLabel;
	public static String CodanPreferencePage_NoInfo;
	public static String CodanPreferencePage_NoParameters;
	public static String ProblemsTreeEditor_NameColumn;
	public static String ProblemsTreeEditor_Problems;
	public static String ProblemsTreeEditor_SeverityColumn;
	public static String OverlayPage_Use_Workspace_Settings;
	public static String OverlayPage_Use_Project_Settings;
	public static String OverlayPage_Configure_Workspace_Settings;
	public static String PropertyStore_Cannot_write_resource_property;
	public static String CustomizeProblemComposite_TabParameters;
	public static String CustomizeProblemComposite_TabScope;
	public static String CustomizeProblemDialog_Message;
	public static String CustomizeProblemDialog_Title;
	public static String Job_TitleRunningAnalysis;
	public static String ParametersComposite_None;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, CodanUIMessages.class);
	}

	private CodanUIMessages() {
	}
}
