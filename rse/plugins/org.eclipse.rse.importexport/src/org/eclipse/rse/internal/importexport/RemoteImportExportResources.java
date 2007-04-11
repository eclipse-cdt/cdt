/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.importexport;

import org.eclipse.osgi.util.NLS;

public class RemoteImportExportResources extends NLS {
	private static String BUNDLE_NAME = "org.eclipse.rse.internal.importexport.RemoteImportExportResources"; //$NON-NLS-1$
	public static String IMPORT_EXPORT_DESCRIPTION_FILE_DIALOG_TITLE;
	public static String IMPORT_EXPORT_DESCRIPTION_FILE_DIALOG_MESSAGE;
	public static String IMPORT_EXPORT_ERROR_DESCRIPTION_ABSOLUTE;
	public static String IMPORT_EXPORT_ERROR_DESCRIPTION_EXISTING_CONTAINER;
	public static String IMPORT_EXPORT_ERROR_DESCRIPTION_NO_CONTAINER;
	public static String IMPORT_EXPORT_ERROR_DESCRIPTION_INVALID_EXTENSION;
	public static String IMPORT_EXPORT_ERROR_CREATE_FILES_FAILED;
	public static String IMPORT_EXPORT_ERROR_CREATE_FILE_FAILED;
	public static String IMPORT_EXPORT_EXPORT_ACTION_DELEGATE_TITLE;
	public static String IMPORT_EXPORT_IMPORT_ACTION_DELEGATE_TITLE;
	public static String IMPORT_EXPORT_ERROR_DESCRIPTION_READ;
	public static String IMPORT_EXPORT_ERROR_DESCRIPTION_CLOSE;
	public static String WizardDataTransfer_existsQuestion;
	public static String WizardDataTransfer_overwriteNameAndPathQuestion;
	public static String WizardDataTransfer_exceptionMessage;
	public static String Question;
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, RemoteImportExportResources.class);
	}
}
