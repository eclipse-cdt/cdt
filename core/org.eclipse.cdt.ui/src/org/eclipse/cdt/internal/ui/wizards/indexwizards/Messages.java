/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.wizards.indexwizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.wizards.indexwizards.messages"; //$NON-NLS-1$
	public static String StringVariableSelectionDialog_columnArgument;
	public static String StringVariableSelectionDialog_columnDescription;
	public static String StringVariableSelectionDialog_message;
	public static String StringVariableSelectionDialog_title;
	public static String TeamProjectIndexExportWizard_title;
	public static String TeamProjectIndexExportWizardPage_description;
	public static String TeamProjectIndexExportWizardPage_deselectAll;
	public static String TeamProjectIndexExportWizardPage_destinationLabel;
	public static String TeamProjectIndexExportWizardPage_destinationMessage;
	public static String TeamProjectIndexExportWizardPage_errorDlgTitle;
	public static String TeamProjectIndexExportWizardPage_errorExporting;
	public static String TeamProjectIndexExportWizardPage_errorInOperation;
	public static String TeamProjectIndexExportWizardPage_labelProjectTable;
	public static String TeamProjectIndexExportWizardPage_noProjectError;
	public static String TeamProjectIndexExportWizardPage_resourceSnapshotButton;
	public static String TeamProjectIndexExportWizardPage_selectAll;
	public static String TeamProjectIndexExportWizardPage_title;
	public static String TeamProjectIndexExportWizardPage_variableButton;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
