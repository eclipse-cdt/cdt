/*******************************************************************************
 * Copyright (c) 2001, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Rational Software - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards;

import org.eclipse.osgi.util.NLS;

public final class NewWizardMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.wizards.NewWizardMessages";//$NON-NLS-1$

	private NewWizardMessages() {
		// Do not instantiate
	}

	public static String AbstractOpenWizardAction_noproject_title;
	public static String AbstractOpenWizardAction_noproject_message;
	public static String AbstractOpenWizardAction_createerror_title;
	public static String AbstractOpenWizardAction_createerror_message;
	public static String NewElementWizard_op_error_title;
	public static String NewElementWizard_op_error_message;
	public static String NewClassWizardPage_files_linkFileButton;
	public static String CreateLinkedResourceGroup_resolvedPathLabel;
	public static String CreateLinkedResourceGroup_browseButton;
	public static String CreateLinkedResourceGroup_open;
	public static String CreateLinkedResourceGroup_targetSelectionLabel;
	public static String CreateLinkedResourceGroup_linkTargetNotFile;
	public static String CreateLinkedResourceGroup_linkTargetNotFolder;
	public static String CreateLinkedResourceGroup_linkTargetNonExistent;
	public static String SourceFolderSelectionDialog_title;
	public static String SourceFolderSelectionDialog_description;

	static {
		NLS.initializeMessages(BUNDLE_NAME, NewWizardMessages.class);
	}
}