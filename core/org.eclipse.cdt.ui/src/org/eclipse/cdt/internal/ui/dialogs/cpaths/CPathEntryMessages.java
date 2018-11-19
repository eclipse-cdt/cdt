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
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import org.eclipse.osgi.util.NLS;

public final class CPathEntryMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.dialogs.cpaths.CPathEntryMessages";//$NON-NLS-1$

	private CPathEntryMessages() {
		// Do not instantiate
	}

	public static String CPathsPropertyPage_no_C_project_message;
	public static String CPathsPropertyPage_closed_project_message;
	public static String CPathsPropertyPage_error_title;
	public static String CPathsPropertyPage_error_message;
	public static String CPathsPropertyPage_unsavedchanges_title;
	public static String CPathsPropertyPage_unsavedchanges_message;
	public static String CPathsPropertyPage_unsavedchanges_button_save;
	public static String CPathsPropertyPage_unsavedchanges_button_discard;
	public static String CPathFilterPage_title;
	public static String CPathFilterPage_description;
	public static String CPathFilterPage_label;
	public static String CPathContainerSelectionPage_title;
	public static String CPathContainerSelectionPage_description;
	public static String CPathContainerWizard_pagecreationerror_title;
	public static String CPathContainerWizard_pagecreationerror_message;
	public static String CPathContainerDefaultPage_title;
	public static String CPathContainerDefaultPage_description;
	public static String CPathContainerDefaultPage_path_label;
	public static String CPathContainerDefaultPage_path_error_enterpath;
	public static String CPathContainerDefaultPage_path_error_invalidpath;
	public static String CPathContainerDefaultPage_path_error_needssegment;
	public static String CPathContainerDefaultPage_path_error_alreadyexists;
	public static String ProjectContainer_label;
	public static String ProjectContainerPage_title;
	public static String ProjectContainerPage_description;
	public static String ProjectContainerPage_label;
	public static String IncludeSymbolEntryPage_title;
	public static String IncludeSymbolEntryPage_label;
	public static String IncludeSymbolEntryPage_addFolderFile;
	public static String IncludeSymbolEntryPage_addUserSymbol;
	public static String IncludeSymbolEntryPage_addExternalInclude;
	public static String IncludeSymbolEntryPage_addFromWorkspace;
	public static String IncludeSymbolEntryPage_addContributed;
	public static String IncludeSymbolEntryPage_edit;
	public static String IncludeSymbolEntryPage_remove;
	public static String IncludeSymbolEntryPage_export;
	public static String IncludeSymbolEntryPage_up;
	public static String IncludeSymbolEntryPage_down;
	public static String IncludeSymbolsEntryPage_show_inherited_check;
	public static String IncludeSymbolEntryPage_addSymbol_title;
	public static String IncludeSymbolEntryPage_addSymbol_message;
	public static String IncludeSymbolEntryPage_editSymbol_title;
	public static String IncludeSymbolEntryPage_editSymbol_message;
	public static String IncludeSymbolEntryPage_addExternal_button_browse;
	public static String IncludeSymbolEntryPage_addExternal_title;
	public static String IncludeSymbolEntryPage_addExternal_message;
	public static String IncludeSymbolEntryPage_editExternal_title;
	public static String IncludeSymbolEntryPage_editExternal_message;
	public static String IncludeSymbolEntryPage_ContainerDialog_new_title;
	public static String IncludeSymbolEntryPage_ContainerDialog_edit_title;
	public static String IncludeSymbolEntryPage_fromWorkspaceDialog_new_title;
	public static String IncludeSymbolEntryPage_fromWorkspaceDialog_new_description;
	public static String IncludeSymbolEntryPage_fromWorkspaceDialog_edit_title;
	public static String IncludeSymbolEntryPage_fromWorkspaceDialog_edit_description;
	public static String IncludeSymbolEntryPage_newResource_title;
	public static String IncludeSymbolEntryPage_newResource_description;
	public static String IncludeSymbolEntryPage_browseForFolder;
	public static String ContainerEntryPage_title;
	public static String ContainerEntryPage_add_button;
	public static String ContainerEntryPage_edit_button;
	public static String ContainerEntryPage_remove_button;
	public static String ContainerEntryPage_export_button;
	public static String ContainerEntryPage_libraries_label;
	public static String ContainerEntryPage_ContainerDialog_new_title;
	public static String ContainerEntryPage_ContainerDialog_edit_title;
	public static String CPathsBlock_path_up_button;
	public static String CPathsBlock_path_down_button;
	public static String CPathsBlock_path_checkall_button;
	public static String CPathsBlock_path_uncheckall_button;
	public static String CPathsBlock_operationdesc_c;
	public static String CPElement_status_multiplePathErrors;
	public static String CPElement_status_pathContainerMissing;
	public static String CPElement_status_libraryPathNotFound;
	public static String CPElement_status_sourcePathMissing;
	public static String CPElement_status_outputPathMissing;
	public static String CPElement_status_notOnSourcePath;
	public static String CPElement_status_includePathNotFound;
	public static String CPElement_status_includeFilePathNotFound;
	public static String CPElement_status_macrosFilePathNotFound;
	public static String CPElement_status_missingProjectPath;
	public static String SourcePathEntryPage_title;
	public static String SourcePathEntryPage_description;
	public static String SourcePathEntryPage_folders_label;
	public static String SourcePathEntryPage_folders_remove_button;
	public static String SourcePathEntryPage_folders_add_button;
	public static String SourcePathEntryPath_folders_edit_button;
	public static String SourcePathEntryPage_ExistingSourceFolderDialog_new_title;
	public static String SourcePathEntryPage_ExistingSourceFolderDialog_new_description;
	public static String SourcePathEntryPage_ExistingSourceFolderDialog_edit_title;
	public static String SourcePathEntryPage_ExistingSourceFolderDialog_edit_description;
	public static String SourcePathEntryPage_NewSourceFolderDialog_new_title;
	public static String SourcePathEntryPage_NewSourceFolderDialog_edit_title;
	public static String SourcePathEntryPage_NewSourceFolderDialog_description;
	public static String SourcePathEntryPage_exclusion_added_title;
	public static String SourcePathEntryPage_exclusion_added_message;
	public static String OutputPathEntryPage_title;
	public static String OutputPathEntryPage_description;
	public static String OutputPathEntryPage_folders_label;
	public static String OutputPathEntryPage_folders_remove_button;
	public static String OutputPathEntryPage_folders_add_button;
	public static String OutputPathEntryPage_folders_edit_button;
	public static String OutputPathEntryPage_ExistingOutputFolderDialog_new_title;
	public static String OutputPathEntryPage_ExistingOutputFolderDialog_new_description;
	public static String OutputPathEntryPage_ExistingOutputFolderDialog_edit_title;
	public static String OutputPathEntryPage_ExistingOutputFolderDialog_edit_description;
	public static String OutputPathEntryPage_exclusion_added_title;
	public static String OutputPathEntryPage_exclusion_added_message;
	public static String ProjectsEntryPage_title;
	public static String ProjectsEntryPage_description;
	public static String ProjectsEntryPage_projects_label;
	public static String ProjectsEntryPage_projects_checkall_button;
	public static String ProjectsEntryWorkbookPage_projects_uncheckall_button;
	public static String LibrariesEntryPage_title;
	public static String LibrariesEntryPage_description;
	public static String LibrariesEntryPage_libraries_label;
	public static String LibrariesEntryPage_libraries_remove_button;
	public static String LibrariesEntryPage_libraries_addextlib_button;
	public static String LibrariesEntryPage_libraries_addcontriblib_button;
	public static String LibrariesEntryPage_libraries_addworkspacelib_button;
	public static String LibrariesEntryPage_libraries_edit_button;
	public static String LibrariesEntryPage_libraries_export_button;
	public static String LibrariesEntryPage_ContainerDialog_new_title;
	public static String LibrariesEntryPage_ContainerDialog_edit_title;
	public static String LibrariesEntryPage_ExtLibDialog_new_title;
	public static String LibrariesEntryPage_ExtLibDialog_new_description;
	public static String LibrariesEntryPage_ExtLibDialog_edit_title;
	public static String LibrariesEntryPage_ExtLibDialog_edit_description;
	public static String OrderExportsPage_title;
	public static String OrderExportsPage_description;
	public static String ExclusionPatternDialog_title;
	public static String ExclusionPatternDialog_pattern_label;
	public static String ExclusionPatternDialog_pattern_add;
	public static String ExclusionPatternDialog_pattern_add_multiple;
	public static String ExclusionPatternDialog_pattern_remove;
	public static String ExclusionPatternDialog_pattern_edit;
	public static String ExclusionPatternDialog_ChooseExclusionPattern_title;
	public static String ExclusionPatternDialog_ChooseExclusionPattern_description;
	public static String ExclusionPatternEntryDialog_add_title;
	public static String ExclusionPatternEntryDialog_edit_title;
	public static String ExclusionPatternEntryDialog_description;
	public static String ExclusionPatternEntryDialog_pattern_label;
	public static String ExclusionPatternEntryDialog_pattern_button;
	public static String ExclusionPatternEntryDialog_error_empty;
	public static String ExclusionPatternEntryDialog_error_notrelative;
	public static String ExclusionPatternEntryDialog_error_exists;
	public static String ExclusionPatternEntryDialog_ChooseExclusionPattern_title;
	public static String ExclusionPatternEntryDialog_ChooseExclusionPattern_description;
	public static String CPElementLabelProvider_new;
	public static String CPElementLabelProvider_willbecreated;
	public static String CPElementLabelProvider_none;
	public static String CPElementLabelProvider_source_attachment_label;
	public static String CPElementLabelProvider_source_attachment_root_label;
	public static String CPElementLabelProvider_exclusion_filter_label;
	public static String CPElementLabelProvider_exclusion_filter_separator;
	public static String CPElementLabelProvider_unknown_element_label;
	public static String CPElementLabelProvider_Includes;
	public static String CPElementLabelProvider_IncludeFiles;
	public static String CPElementLabelProvider_PreprocessorSymbols;
	public static String CPElementLabelProvider_MacrosFiles;
	public static String CPElementLabelProvider_Libraries;
	public static String CPElementLabelProvider_export_label;
	public static String NewSourceFolderDialog_useproject_button;
	public static String NewSourceFolderDialog_usefolder_button;
	public static String NewSourceFolderDialog_sourcefolder_label;
	public static String NewSourceFolderDialog_error_invalidpath;
	public static String NewSourceFolderDialog_error_enterpath;
	public static String NewSourceFolderDialog_error_pathexists;
	public static String FolderSelectionDialog_button;
	public static String MultipleFolderSelectionDialog_button;
	public static String SourceAttachmentBlock_message;
	public static String SourceAttachmentBlock_filename_label;
	public static String SourceAttachmentBlock_filename_externalfile_button;
	public static String SourceAttachmentBlock_filename_externalfolder_button;
	public static String SourceAttachmentBlock_filename_internal_button;
	public static String SourceAttachmentBlock_filename_error_notvalid;
	public static String SourceAttachmentBlock_filename_error_filenotexists;
	public static String SourceAttachmentBlock_intjardialog_title;
	public static String SourceAttachmentBlock_intjardialog_message;
	public static String SourceAttachmentBlock_extjardialog_text;
	public static String SourceAttachmentBlock_extfolderdialog_text;
	public static String SourceAttachmentBlock_putoncpdialog_title;
	public static String SourceAttachmentBlock_putoncpdialog_message;
	public static String SourceAttachmentDialog_title;
	public static String SourceAttachmentDialog_error_title;
	public static String SourceAttachmentDialog_error_message;

	static {
		NLS.initializeMessages(BUNDLE_NAME, CPathEntryMessages.class);
	}
}