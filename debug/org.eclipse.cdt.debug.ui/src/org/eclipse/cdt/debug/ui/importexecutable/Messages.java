/*******************************************************************************
 * Copyright (c) 2006 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.importexecutable;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.debug.ui.importexecutable.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String ImportExecutableWizard_pageOneTitle;

	public static String ImportExecutableWizard_pageOneDescription;

	public static String ImportExecutableWizard_executableListLabel;

	public static String ImportExecutableWizard_fileDialogTitle;

	public static String ImportExecutableWizard_AllFiles;

	public static String ImportExecutableWizard_Applications;

	public static String ImportExecutableWizard_LIbaries;

	public static String ImportExecutablePageOne_SearchDirectory;

	public static String ImportExecutablePageOne_SelectExecutable;

	public static String ImportExecutablePageOne_NoteAnEXE;

	public static String ImportExecutablePageOne_NoSuchFile;

	public static String ImportExecutablePageOne_Browse;

	public static String ImportExecutablePageOne_SelectAll;

	public static String ImportExecutablePageOne_DeselectAll;

	public static String ImportExecutablePageOne_SelectADirectory;

	public static String ImportExecutablePageOne_Searching;

	public static String ImportExecutablePageOne_ProcessingResults;

	public static String ImportExecutablePageTwo_ChooseProject;

	public static String ImportExecutablePageTwo_ChooseExisting;

	public static String ImportExecutablePageTwo_BadProjectName;

	public static String ImportExecutablePageTwo_EnterLaunchConfig;

	public static String ImportExecutablePageTwo_EnterProjectName;

	public static String ImportExecutablePageTwo_ProjectAlreadyExists;

	public static String ImportExecutablePageTwo_NewProjectName;

	public static String ImportExecutablePageTwo_ProjectLabel;

	public static String ImportExecutablePageTwo_ExistingProject;

	public static String ImportExecutablePageTwo_Search;

	public static String ImportExecutablePageTwo_CreateLaunch;

	public static String ImportExecutablePageTwo_Name;

	public static String ImportExecutablePageTwo_DefaultProjectPrefix;

	public static String AbstractImportExecutableWizard_windowTitle;

	public static String AbstractImportExecutableWizard_CreateLaunchConfiguration;
}
