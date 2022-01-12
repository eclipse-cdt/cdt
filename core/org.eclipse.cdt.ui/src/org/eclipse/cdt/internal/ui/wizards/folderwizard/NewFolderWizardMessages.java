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
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.folderwizard;

import org.eclipse.osgi.util.NLS;

public final class NewFolderWizardMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.wizards.folderwizard.NewFolderWizardMessages";//$NON-NLS-1$

	private NewFolderWizardMessages() {
		// Do not instantiate
	}

	public static String NewSourceFolderCreationWizard_title;
	public static String NewSourceFolderWizardPage_title;
	public static String NewSourceFolderWizardPage_description;
	public static String NewSourceFolderWizardPage_root_label;
	public static String NewSourceFolderWizardPage_root_button;
	public static String NewSourceFolderWizardPage_project_label;
	public static String NewSourceFolderWizardPage_project_button;
	public static String NewSourceFolderWizardPage_operation;
	public static String NewSourceFolderWizardPage_exclude_label;
	public static String NewSourceFolderWizardPage_ChooseExistingRootDialog_title;
	public static String NewSourceFolderWizardPage_ChooseExistingRootDialog_description;
	public static String NewSourceFolderWizardPage_ChooseProjectDialog_title;
	public static String NewSourceFolderWizardPage_ChooseProjectDialog_description;
	public static String NewSourceFolderWizardPage_error_EnterRootName;
	public static String NewSourceFolderWizardPage_error_InvalidRootName;
	public static String NewSourceFolderWizardPage_error_NotAFolder;
	public static String NewSourceFolderWizardPage_error_AlreadyExisting;
	public static String NewSourceFolderWizardPage_error_EnterProjectName;
	public static String NewSourceFolderWizardPage_error_InvalidProjectPath;
	public static String NewSourceFolderWizardPage_error_NotACProject;
	public static String NewSourceFolderWizardPage_error_ProjectNotExists;
	public static String NewSourceFolderWizardPage_warning_ReplaceSF;
	public static String NewSourceFolderWizardPage_warning_AddedExclusions;

	static {
		NLS.initializeMessages(BUNDLE_NAME, NewFolderWizardMessages.class);
	}
}