/*******************************************************************************
 * Copyright (c) 2001, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rational Software - initial implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;

import org.eclipse.osgi.util.NLS;

public final class CUIMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.CUIMessages";//$NON-NLS-1$

	private CUIMessages() {
		// Do not instantiate
	}

	public static String Drag_move_problem_title;
	public static String Drag_move_problem_message;
	public static String ExceptionDialog_seeErrorLogMessage;
	public static String CAnnotationHover_multipleMarkers;
	public static String TabFolderOptionBlock_error;
	public static String TabFolderOptionBlock_error_settingOptions;
	public static String TabFolderOptionBlock_error_message;
	public static String BinaryParserBlock_binaryParser;
	public static String BinaryParserBlock_button_up;
	public static String BinaryParserBlock_button_down;
	public static String BinaryParserBlock_binaryParserOptions;
	public static String BinaryParserBlock_settingBinaryParser;
	public static String ReferenceBlock_task_ReferenceProjects;
	public static String BinaryParserPage_task_savingAttributes;
	public static String BinaryParserPage_label_addr2lineCommand;
	public static String BinaryParserPage_label_browse;
	public static String BinaryParserPage_label_browse1;
	public static String BinaryParserPage_label_browse2;
	public static String BinaryParserPage_label_cppfiltCommand;
	public static String BinaryParserPage_label_cygpathCommand;
	public static String BinaryParserPage_label_nmCommand;
	public static String AbstractErrorParserBlock_task_setErrorParser;
	public static String ConvertProjectWizardPage_convertTo;
	public static String ConvertProjectWizardPage_SelectAll;
	public static String ConvertProjectWizardPage_CProject;
	public static String ConvertProjectWizardPage_CppProject;
	public static String ConvertProjectWizardPage_DeselectAll;
	public static String AbstractErrorParserBlock_label_up;
	public static String AbstractErrorParserBlock_label_down;
	public static String AbstractErrorParserBlock_label_selectAll;
	public static String AbstractErrorParserBlock_label_unselectAll;
	public static String AbstractErrorParserBlock_label_errorParsers;
	public static String ICElementPropertyConstants_catagory;
	public static String StatusBarUpdater_num_elements_selected;
	public static String CHelpConfigurationPropertyPage_buttonLabels_CheckAll;
	public static String CHelpConfigurationPropertyPage_buttonLabels_UncheckAll;
	public static String CHelpConfigurationPropertyPage_HelpBooks;
	public static String AsyncTreeContentProvider_JobName;
	public static String AsyncTreeContentProvider_TaskName;
	public static String TextEditorDropAdapter_error_title;
	public static String TextEditorDropAdapter_error_message;
	public static String TextEditorDropAdapter_unreadableFile;
	public static String TextEditorDropAdapter_noFile;
	public static String OptionalMessageDialog_dontShowAgain;
	public static String OptionalMessageDialog_rememberDecision;
	public static String CStructureCreatorVisitor_translationUnitName;
	public static String FileTransferDragAdapter_refreshing;
	public static String FileTransferDragAdapter_problem;
	public static String FileTransferDragAdapter_problemTitle;

	static {
		NLS.initializeMessages(BUNDLE_NAME, CUIMessages.class);
	}
}