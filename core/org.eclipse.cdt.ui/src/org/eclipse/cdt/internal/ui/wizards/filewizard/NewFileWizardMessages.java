/*******************************************************************************
 * Copyright (c) 2004, 2008 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.filewizard;

import org.eclipse.osgi.util.NLS;

public final class NewFileWizardMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.wizards.filewizard.NewFileWizardMessages";//$NON-NLS-1$

	private NewFileWizardMessages() {
		// Do not instantiate
	}

	public static String AbstractFileCreationWizard_title;
	public static String AbstractFileCreationWizardPage_description;
	public static String AbstractFileCreationWizardPage_sourceFolder_label;
	public static String AbstractFileCreationWizardPage_sourceFolder_button;
	public static String AbstractFileCreationWizardPage_error_EnterSourceFolderName;
	public static String AbstractFileCreationWizardPage_error_NotAFolder;
	public static String AbstractFileCreationWizardPage_error_NotASourceFolder;
	public static String AbstractFileCreationWizardPage_error_FolderDoesNotExist;
	public static String AbstractFileCreationWizardPage_template_label;
	public static String AbstractFileCreationWizardPage_configure_label;
	public static String AbstractFileCreationWizardPage_noTemplate;
	public static String AbstractFileCreationWizardPage_warning_NotACProject;
	public static String AbstractFileCreationWizardPage_warning_NotInACProject;
	public static String NewHeaderFileCreationWizard_title;
	public static String NewSourceFileCreationWizard_title;
	public static String NewHeaderFileCreationWizardPage_title;
	public static String NewHeaderFileCreationWizardPage_description;
	public static String NewHeaderFileCreationWizardPage_headerFile_label;
	public static String NewHeaderFileCreationWizardPage_error_EnterFileName;
	public static String NewHeaderFileCreationWizardPage_error_FileNotInSourceFolder;
	public static String NewHeaderFileCreationWizardPage_error_FileExists;
	public static String NewHeaderFileCreationWizardPage_error_MatchingFolderExists;
	public static String NewHeaderFileCreationWizardPage_error_MatchingResourceExists;
	public static String NewHeaderFileCreationWizardPage_error_FolderDoesNotExist;
	public static String NewHeaderFileCreationWizardPage_warning_FileNameDiscouraged;
	public static String NewHeaderFileCreationWizardPage_error_InvalidFileName;
	public static String NewSourceFileCreationWizardPage_title;
	public static String NewSourceFileCreationWizardPage_description;
	public static String NewSourceFileCreationWizardPage_sourceFile_label;
	public static String NewSourceFileCreationWizardPage_error_EnterFileName;
	public static String NewSourceFileCreationWizardPage_error_FileNotInSourceFolder;
	public static String NewSourceFileCreationWizardPage_error_FileExists;
	public static String NewSourceFileCreationWizardPage_error_MatchingFolderExists;
	public static String NewSourceFileCreationWizardPage_error_MatchingResourceExists;
	public static String NewSourceFileCreationWizardPage_error_FolderDoesNotExist;
	public static String NewSourceFileCreationWizardPage_warning_FileNameDiscouraged;
	public static String NewSourceFileCreationWizardPage_error_InvalidFileName;
	public static String NewSourceFileGenerator_createFile_task;
	public static String NewFileFromTemplateWizard_pageTitle;
	public static String NewFileFromTemplateWizard_description;
	public static String NewFileFromTemplateWizard_shellTitle;
	public static String NewFileFromTemplateWizard_errorMessage;
	public static String WizardNewFileFromTemplateCreationPage_configure_label;
	public static String WizardNewFileFromTemplateCreationPage_noTemplate_name;
	public static String WizardNewFileFromTemplateCreationPage_useTemplate_label;

	static {
		NLS.initializeMessages(BUNDLE_NAME, NewFileWizardMessages.class);
	}
}