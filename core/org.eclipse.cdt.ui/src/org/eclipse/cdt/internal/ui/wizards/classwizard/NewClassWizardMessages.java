/*******************************************************************************
 * Copyright (c) 2004, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import org.eclipse.osgi.util.NLS;

public final class NewClassWizardMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.wizards.classwizard.NewClassWizardMessages";//$NON-NLS-1$

	private NewClassWizardMessages() {
		// Do not instantiate
	}

	public static String NewClassCreationWizard_title;
	public static String NewClassCreationWizardPage_title;
	public static String NewClassCreationWizardPage_description;
	public static String NewClassCreationWizardPage_getTypes_noClasses_title;
	public static String NewClassCreationWizardPage_getTypes_noClasses_message;
	public static String NewClassCreationWizardPage_getTypes_noNamespaces_title;
	public static String NewClassCreationWizardPage_getTypes_noNamespaces_message;
	public static String NewClassCreationWizardPage_sourceFolder_label;
	public static String NewClassCreationWizardPage_sourceFolder_button;
	public static String NewClassCreationWizardPage_error_EnterSourceFolderName;
	public static String NewClassCreationWizardPage_error_NotAFolder;
	public static String NewClassCreationWizardPage_error_NotASourceFolder;
	public static String NewClassCreationWizardPage_warning_NotACProject;
	public static String NewClassCreationWizardPage_warning_NotInACProject;
	public static String NewClassCreationWizardPage_namespace_label;
	public static String NewClassCreationWizardPage_namespace_button;
	public static String NewClassCreationWizardPage_error_EnterNamespace;
	public static String NewClassCreationWizardPage_error_EnclosingNamespaceNotExists;
	public static String NewClassCreationWizardPage_error_NamespaceExistsDifferentCase;
	public static String NewClassCreationWizardPage_error_TypeMatchingNamespaceExists;
	public static String NewClassCreationWizardPage_error_TypeMatchingNamespaceExistsDifferentCase;
	public static String NewClassCreationWizardPage_warning_NamespaceNotExists;
	public static String NewClassCreationWizardPage_error_InvalidNamespace;
	public static String NewClassCreationWizardPage_warning_NamespaceDiscouraged;
	public static String NewClassCreationWizardPage_className_label;
	public static String NewClassCreationWizardPage_error_EnterClassName;
	public static String NewClassCreationWizardPage_error_ClassNameExists;
	public static String NewClassCreationWizardPage_error_ClassNameExistsDifferentCase;
	public static String NewClassCreationWizardPage_error_TypeMatchingClassExists;
	public static String NewClassCreationWizardPage_error_TypeMatchingClassExistsDifferentCase;
	public static String NewClassCreationWizardPage_error_InvalidClassName;
	public static String NewClassCreationWizardPage_error_QualifiedClassName;
	public static String NewClassCreationWizardPage_warning_ClassNameDiscouraged;
	public static String NewClassCreationWizardPage_baseClasses_label;
	public static String NewClassCreationWizardPage_error_BaseClassNotExistsInProject;
	public static String NewClassCreationWizardPage_methodStubs_label;
	public static String NewClassCreationWizardPage_error_NotAFile;
	public static String NewClassCreationWizardPage_error_FolderDoesNotExist;
	public static String NewClassCreationWizardPage_useDefaultLocation_label;
	public static String NewClassCreationWizardPage_headerFile_label;
	public static String NewClassCreationWizardPage_headerFile_button;
	public static String NewClassCreationWizardPage_ChooseHeaderFileDialog_title;
	public static String NewClassCreationWizardPage_error_EnterHeaderFileName;
	public static String NewClassCreationWizardPage_error_HeaderFileNotInSourceFolder;
	public static String NewClassCreationWizardPage_warning_HeaderFileNameDiscouraged;
	public static String NewClassCreationWizardPage_warning_HeaderFileExists;
	public static String NewClassCreationWizardPage_error_InvalidHeaderFileName;
	public static String NewClassCreationWizardPage_sourceFile_button;
	public static String NewClassCreationWizardPage_sourceFile_label;
	public static String NewClassCreationWizardPage_ChooseSourceFileDialog_title;
	public static String NewClassCreationWizardPage_error_EnterSourceFileName;
	public static String NewClassCreationWizardPage_error_SourceFileNotInSourceFolder;
	public static String NewClassCreationWizardPage_warning_SourceFileNameDiscouraged;
	public static String NewClassCreationWizardPage_warning_SourceFileExists;
	public static String NewClassCreationWizardPage_error_InvalidSourceFileName;
	public static String NewClassCreationWizardPage_error_LocationUnknown;
	public static String BaseClassesListDialogField_buttons_add;
	public static String BaseClassesListDialogField_buttons_remove;
	public static String BaseClassesListDialogField_buttons_up;
	public static String BaseClassesListDialogField_buttons_down;
	public static String BaseClassesListDialogField_headings_name;
	public static String BaseClassesListDialogField_headings_access;
	public static String BaseClassesListDialogField_headings_virtual;
	public static String BaseClassesLabelProvider_boolean_yes_label;
	public static String BaseClassesLabelProvider_boolean_no_label;
	public static String BaseClassesLabelProvider_access_public_label;
	public static String BaseClassesLabelProvider_access_protected_label;
	public static String BaseClassesLabelProvider_access_private_label;
	public static String MethodStubsDialogField_headings_name;
	public static String MethodStubsDialogField_headings_access;
	public static String MethodStubsDialogField_headings_virtual;
	public static String MethodStubsDialogField_headings_inline;
	public static String NamespaceSelectionDialog_title;
	public static String NamespaceSelectionDialog_message;
	public static String EnclosingClassSelectionDialog_title;
	public static String EnclosingClassSelectionDialog_message;
	public static String NewBaseClassSelectionDialog_title;
	public static String NewBaseClassSelectionDialog_message;
	public static String NewBaseClassSelectionDialog_addButton_label;
	public static String NewBaseClassSelectionDialog_classadded_info;
	public static String NewBaseClassSelectionDialog_classalreadyadded_info;
	public static String NewBaseClassSelectionDialog_addingclass_info;
	public static String NewBaseClassSelectionDialog_error_classnotadded;
	public static String SourceFileSelectionDialog_folderName_label;
	public static String SourceFileSelectionDialog_fileName_label;
	public static String SourceFileSelectionDialog_error_EnterFolderName;
	public static String SourceFileSelectionDialog_error_FolderDoesNotExist;
	public static String SourceFileSelectionDialog_error_NotAFolder;
	public static String SourceFileSelectionDialog_error_NotASourceFolder;
	public static String SourceFileSelectionDialog_warning_NotACProject;
	public static String SourceFileSelectionDialog_warning_NotInACProject;
	public static String SourceFileSelectionDialog_error_EnterFileName;
	public static String SourceFileSelectionDialog_error_NotAFile;
	public static String SourceFileSelectionDialog_error_NotASourceFile;
	public static String SourceFileSelectionDialog_warning_SourceFileExists;
	public static String NewClassCodeGeneration_createType_mainTask;
	public static String NewClassCodeGeneration_createType_task_header;
	public static String NewClassCodeGeneration_createType_task_header_includePaths;
	public static String NewClassCodeGeneration_createType_task_header_addIncludePaths;
	public static String NewClassCodeGeneration_createType_task_source;
	public static String NewClassCodeGeneration_stub_constructor_name;
	public static String NewClassCodeGeneration_stub_destructor_name;

	static {
		NLS.initializeMessages(BUNDLE_NAME, NewClassWizardMessages.class);
	}
}