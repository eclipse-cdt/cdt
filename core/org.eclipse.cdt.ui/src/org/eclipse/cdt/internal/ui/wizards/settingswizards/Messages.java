/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.settingswizards;

import org.eclipse.osgi.util.NLS;


class Messages extends NLS {
	
	private static final String BUNDLE_NAME = 
		"org.eclipse.cdt.internal.ui.wizards.settingswizards.messages"; //$NON-NLS-1$

	static {
		initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages() { }
	
	
	// messages specific to the import page
	public static String 
		ProjectSettingsWizardPage_Import_title,
		ProjectSettingsWizardPage_Import_message,
		ProjectSettingsWizardPage_Import_selectSettings,
		ProjectSettingsWizardPage_Import_checkBox,
		ProjectSettingsWizardPage_Import_file,
		ProjectSettingsWizardPage_Import_parseError,
		ProjectSettingsWizardPage_Import_openError;
		
	// messages specific to the export page
	public static String
		ProjectSettingsWizardPage_Export_title,
		ProjectSettingsWizardPage_Export_message,
		ProjectSettingsWizardPage_Export_selectSettings,
		ProjectSettingsWizardPage_Export_checkBox,
		ProjectSettingsWizardPage_Export_file;
	
	// messages common to both
	public static String
		ProjectSettingsWizardPage_selectAll,
		ProjectSettingsWizardPage_deselectAll,
		ProjectSettingsWizardPage_selectProject,
		ProjectSettingsWizardPage_browse,
		ProjectSettingsWizardPage_noOpenProjects,
		ProjectSettingsWizardPage_selectConfiguration,
		ProjectSettingsWizardPage_active;
	
	// messages for settings processors
	public static String
		ProjectSettingsWizardPage_Processor_Includes,
		ProjectSettingsWizardPage_Processor_Macros;
	
	// error messages during export
	public static String 
		ProjectSettingsExportStrategy_couldNotOpen,
		ProjectSettingsExportStrategy_exportError,
		ProjectSettingsExportStrategy_exportFailed,
		ProjectSettingsExportStrategy_fileOpenError,
		ProjectSettingsExportStrategy_xmlError;
	
	// error messages during import
	public static String 
		ProjectSettingsImportStrategy_couldNotImport,
		ProjectSettingsImportStrategy_couldNotOpen,
		ProjectSettingsImportStrategy_fileOpenError,
		ProjectSettingsImportStrategy_importError,
		ProjectSettingsImportStrategy_saveError;
	
}
