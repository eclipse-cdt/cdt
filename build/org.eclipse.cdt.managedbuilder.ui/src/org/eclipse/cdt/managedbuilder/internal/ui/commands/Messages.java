/*******************************************************************************
 * Copyright (c) 2010, 2014 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *     IBM Corporation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui.commands;

import org.eclipse.osgi.util.NLS;

class Messages extends NLS {
	public static String BuildConfigurationsJob_BuildError;
	public static String BuildConfigurationsJob_Building;
	public static String BuildConfigurationsJob_Cleaning;
	public static String BuildFilesHandler_buildingSelectedFiles;
	public static String CleanFilesHandler_cleaningFiles;
	public static String RebuildConfigurationsDialog_Active;
	public static String RebuildConfigurationsDialog_BuildConfigurations;
	public static String RebuildConfigurationsDialog_CleanConfigurations;
	public static String RebuildConfigurationsDialog_RebuildConfigurations;
	public static String RebuildConfigurationsDialog_SelectConfigurations;
	public static String ConvertTargetHandler_No_Converter;
	public static String ProjectConvert_noConverterErrorDialog_message;
	public static String ProjectConvert_title;
	public static String ProjectConvert_conversionErrordialog_message;
	public static String ProjectConvert_conversionErrordialog_title;
	public static String ProjectConvert_convertersList;

	static {
		// Initialize resource bundle.
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
