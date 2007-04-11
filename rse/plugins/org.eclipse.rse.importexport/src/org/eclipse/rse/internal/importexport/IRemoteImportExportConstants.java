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

/**
 * Interface containing contstants required for import and export.
 */
public interface IRemoteImportExportConstants {
	/**
	 * Remote file import description file extension, "rimpfd".
	 */
	public static final String REMOTE_FILE_IMPORT_DESCRIPTION_FILE_EXTENSION = "rimpfd"; //$NON-NLS-1$
	/**
	 * An array of import description file extensions.
	 */
	public static final String[] REMOTE_IMPORT_DESCRIPTION_FILE_EXTENSIONS = { REMOTE_FILE_IMPORT_DESCRIPTION_FILE_EXTENSION };
	/**
	 * Remote file export description file extension, "rexpfd".
	 */
	public static final String REMOTE_FILE_EXPORT_DESCRIPTION_FILE_EXTENSION = "rexpfd"; //$NON-NLS-1$
	/**
	 * Remote jar export description file extension, "rmtjardesc".
	 */
	public static final String REMOTE_JAR_EXPORT_DESCRIPTION_FILE_EXTENSION = "rmtjardesc"; //$NON-NLS-1$
	/**
	 * An array of export description file extensions.
	 */
	public static final String[] REMOTE_EXPORT_DESCRIPTION_FILE_EXTENSIONS = { REMOTE_FILE_EXPORT_DESCRIPTION_FILE_EXTENSION, REMOTE_JAR_EXPORT_DESCRIPTION_FILE_EXTENSION };
}
