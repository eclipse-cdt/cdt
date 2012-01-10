/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * 	   Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	public static String DeleteFileChange_0;
	public static String DeleteFileChange_1;
	public static String Refactoring_name;
	public static String Refactoring_PM_LoadTU;
	public static String Refactoring_PM_CheckTU;
	public static String Refactoring_PM_InitRef;
	public static String Refactoring_PM_ParseTU;
	public static String Refactoring_PM_MergeComments;
	public static String Refactoring_CanceledByUser;
	public static String Refactoring_CompileErrorInTU;
	public static String AddDeclarationNodeToClassChange_AddDeclaration;
	public static String CreateFileChange_CreateFile;
	public static String CreateFileChange_UnknownLoc;
	public static String CreateFileChange_FileExists;
	public static String CRefactoring_FileNotFound;
	public static String CRefactoring_checking_final_conditions;
	public static String Refactoring_SelectionNotValid;
	public static String Refactoring_CantLoadTU;
	public static String Refactoring_Ambiguity;
	public static String Refactoring_ParsingError;
	public static String NO_FILE;
	public static String RefactoringSaveHelper_unexpected_exception;
	public static String RefactoringSaveHelper_saving;
	public static String RefactoringSaveHelper_always_save;
	public static String RefactoringSaveHelper_save_all_resources;
	public static String RefactoringSaveHelper_must_save;
	public static String ChangeExceptionHandler_abort_button;
	public static String ChangeExceptionHandler_dialog_message;
	public static String ChangeExceptionHandler_dialog_title;
	public static String ChangeExceptionHandler_message;
	public static String ChangeExceptionHandler_status_without_detail;
	public static String ChangeExceptionHandler_undo_button;
	public static String ChangeExceptionHandler_undo_dialog_message;
	public static String ChangeExceptionHandler_undo_dialog_title;
	public static String RefactoringExecutionHelper_cannot_execute;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	// Do not instantiate
	private Messages() {
	}
}
