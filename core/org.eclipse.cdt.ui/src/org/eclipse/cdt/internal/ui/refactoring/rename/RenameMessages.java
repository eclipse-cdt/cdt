/*******************************************************************************
 * Copyright (c) 2004, 2018 Wind River Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import org.eclipse.osgi.util.NLS;

class RenameMessages extends NLS {
	public static String ASTManager_error_macro_name_conflict;
	public static String ASTManager_subtask_analyzing;
	public static String ASTManager_task_analyze;
	public static String ASTManager_task_generateAst;
	public static String ASTManager_warning_parsingError_detailed;
	public static String ASTManager_warning_parsingError_withFile;
	public static String ASTManager_warning_parsingError_withFileAndLine;
	public static String ASTManager_warning_parsingError;
	public static String CRefactoringMatch_label_inComment;
	public static String CRefactoringMatch_label_occurrences;
	public static String CRefactoringMatch_label_potentialOccurrences;
	public static String CRefactory_title_rename;
	public static String CRenameIncludeProcessor_includeDirective;
	public static String CRenameLocalProcessor_constructor;
	public static String CRenameLocalProcessor_enumerator;
	public static String CRenameLocalProcessor_error_conflict;
	public static String CRenameLocalProcessor_error_isShadowed;
	public static String CRenameLocalProcessor_error_message;
	public static String CRenameLocalProcessor_error_message1;
	public static String CRenameLocalProcessor_error_message2;
	public static String CRenameLocalProcessor_error_message3;
	public static String CRenameLocalProcessor_error_overloads;
	public static String CRenameLocalProcessor_error_redeclare;
	public static String CRenameLocalProcessor_error_shadow;
	public static String CRenameLocalProcessor_field;
	public static String CRenameLocalProcessor_globalVariable;
	public static String CRenameLocalProcessor_localVariable;
	public static String CRenameLocalProcessor_method;
	public static String CRenameLocalProcessor_parameter;
	public static String CRenameMethodProcessor_fatalError_renameConstructor;
	public static String CRenameMethodProcessor_fatalError_renameDestructor;
	public static String CRenameMethodProcessor_fatalError_renameOperator;
	public static String CRenameMethodProcessor_fatalError_renameToConstructor;
	public static String CRenameMethodProcessor_fatalError_renameToDestructor;
	public static String CRenameMethodProcessor_warning_illegalCharacters;
	public static String CRenameMethodProcessor_warning_renameVirtual;
	public static String CRenameProcessorDelegate_fileStaticFunction;
	public static String CRenameProcessorDelegate_fileStaticVariable;
	public static String CRenameProcessorDelegate_globalFunction;
	public static String CRenameProcessorDelegate_namespace;
	public static String CRenameProcessorDelegate_task_checkFinalCondition;
	public static String CRenameProcessorDelegate_task_createChange;
	public static String CRenameProcessorDelegate_type;
	public static String CRenameProcessorDelegate_warning_commentMatch_plural;
	public static String CRenameProcessorDelegate_warning_commentMatch_singular;
	public static String CRenameProcessorDelegate_warning_potentialMatch_plural;
	public static String CRenameProcessorDelegate_warning_potentialMatch_singular;
	public static String CRenameProcessorDelegate_wizard_title;
	public static String CRenameRefactoringInputPage_button_chooseWorkingSet;
	public static String CRenameRefactoringInputPage_button_comments;
	public static String CRenameRefactoringInputPage_button_sourceCode;
	public static String CRenameRefactoringInputPage_button_inactiveCode;
	public static String CRenameRefactoringInputPage_button_includes;
	public static String CRenameRefactoringInputPage_button_macroDefinitions;
	public static String CRenameRefactoringInputPage_button_preprocessor;
	public static String CRenameRefactoringInputPage_button_strings;
	public static String CRenameRefactoringInputPage_button_exhaustiveFileSearch;
	public static String CRenameRefactoringInputPage_button_singleProject;
	public static String CRenameRefactoringInputPage_button_relatedProjects;
	public static String CRenameRefactoringInputPage_button_workspace;
	public static String CRenameRefactoringInputPage_button_workingSet;
	public static String CRenameRefactoringInputPage_errorInvalidIdentifier;
	public static String CRenameRefactoringInputPage_label_newName;
	public static String CRenameRefactoringInputPage_label_updateWithin;
	public static String CRenameRefactoringInputPage_renameBaseAndDerivedMethods;
	public static String CRenameTopProcessor_enumerator;
	public static String CRenameTopProcessor_error_invalidName;
	public static String CRenameTopProcessor_error_invalidTextSelection;
	public static String CRenameTopProcessor_error_renameWithoutSourceFile;
	public static String CRenameTopProcessor_field;
	public static String CRenameTopProcessor_filelocalFunction;
	public static String CRenameTopProcessor_filelocalVar;
	public static String CRenameTopProcessor_globalFunction;
	public static String CRenameTopProcessor_globalVar;
	public static String CRenameTopProcessor_localVar;
	public static String CRenameTopProcessor_macro;
	public static String CRenameTopProcessor_method;
	public static String CRenameTopProcessor_namespace;
	public static String CRenameTopProcessor_parameter;
	public static String CRenameTopProcessor_type;
	public static String CRenameTopProcessor_virtualMethod;
	public static String CRenameTopProcessor_wizard_backup_title;
	public static String CRenameTopProcessor_wizard_title;
	public static String CResourceRenameRefactoringInputPage_new_name;
	public static String CResourceRenameRefactoringInputPage_open_preferences;
	public static String CResourceRenameRefactoringInputPage_open_preferences_tooltip;
	public static String CResourceRenameRefactoringInputPage_update_references;
	public static String HeaderFileMoveParticipant_name;
	public static String HeaderFileRenameParticipant_name;
	public static String HeaderReferenceAdjuster_update_include_guards;
	public static String HeaderReferenceAdjuster_update_includes;
	public static String HeaderReferenceAdjuster_update_include_guards_and_includes;
	public static String RenameCSourceFolderChange_ErrorMsg;
	public static String RenameCSourceFolderChange_Name0;
	public static String SourceFolderRenameParticipant_name;
	public static String RenameInformationPopup_delayJobName;
	public static String RenameInformationPopup_EnterNewName;
	public static String RenameInformationPopup_menu;
	public static String RenameInformationPopup_OpenDialog;
	public static String RenameInformationPopup_preferences;
	public static String RenameInformationPopup_Preview;
	public static String RenameInformationPopup_RenameInWorkspace;
	public static String RenameInformationPopup_snap_bottom_right;
	public static String RenameInformationPopup_snap_over_left;
	public static String RenameInformationPopup_snap_over_right;
	public static String RenameInformationPopup_snap_under_left;
	public static String RenameInformationPopup_snap_under_right;
	public static String RenameInformationPopup_SnapTo;
	public static String RenameLinkedMode_error_saving_editor;
	public static String RenameSupport_not_available;
	public static String RenameSupport_dialog_title;
	public static String RenameSupport_rename_resource;
	public static String TextSearch_monitor_categorizeMatches;

	static {
		NLS.initializeMessages(RenameMessages.class.getName(), RenameMessages.class);
	}

	// Do not instantiate.
	private RenameMessages() {
	}
}
