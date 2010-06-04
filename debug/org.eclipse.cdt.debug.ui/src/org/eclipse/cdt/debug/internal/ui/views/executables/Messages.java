/*******************************************************************************
 * Copyright (c) 2008, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.executables;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.debug.internal.ui.views.executables.messages"; //$NON-NLS-1$
	public static String ExecutablesContentProvider_FetchingExecutables;
	public static String ExecutablesView_Columns;
	public static String ExecutablesView_ConfigureColumns;
	public static String ExecutablesView_ConfirmRemoveExe;
	public static String ExecutablesView_ConfirmRemoveSelected;
	public static String ExecutablesView_ExeData;
	public static String ExecutablesView_ExeLocation;
	public static String ExecutablesView_ExeName;
	public static String ExecutablesView_ExeProject;
	public static String ExecutablesView_ExeSize;
	public static String ExecutablesView_ExeType;
	public static String ExecutablesView_Finding_Sources_Job_Name;
	public static String ExecutablesView_Import;
	public static String ExecutablesView_ImportExe;
	public static String ExecutablesView_ImportExecutables;
	public static String ExecutablesView_Refresh;
	public static String ExecutablesView_RefreshList;
	public static String ExecutablesView_Remove;
	public static String ExecutablesView_RemoveExes;
	public static String ExecutablesView_RemoveSelectedExes;
	public static String ExecutablesView_Select_Executable;
	public static String ExecutablesView_SelectColumns;
	public static String ExecutablesView_SelectExeFile;
	public static String ExecutablesView_SrcDate;
	public static String ExecutablesView_SrcLocation;
	public static String ExecutablesView_SrcName;
	public static String ExecutablesView_SrcOrgLocation;
	public static String ExecutablesView_SrcSize;
	public static String ExecutablesView_SrcType;
	public static String ExecutablesViewer_ExecutableName;
	public static String ExecutablesViewer_Location;
	public static String ExecutablesViewer_Modified;
	public static String ExecutablesViewer_Project;
	public static String ExecutablesViewer_RefreshExecutablesView;
	public static String ExecutablesViewer_Size;
	public static String ExecutablesViewer_Type;
	public static String SourceFilesContentProvider_NoFilesFound;
	public static String SourceFilesContentProvider_ReadingDebugSymbolInformationLabel;
	public static String SourceFilesContentProvider_Refreshing;
	public static String SourceFilesViewer_RefreshSourceFiles;
	public static String SourceFilesViewer_Location;
	public static String SourceFilesViewer_Modified;
	public static String SourceFilesViewer_Original;
	public static String SourceFilesViewer_Size;
	public static String SourceFilesViewer_SourceFileName;
	public static String SourceFilesViewer_Type;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
