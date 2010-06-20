/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
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
	public static String Refactoring_SelectionNotValid;
	public static String Refactoring_CantLoadTU;
	public static String Refactoring_Ambiguity;
	public static String NodeContainer_Name;
	public static String NO_FILE;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	// Do not instantiate
	private Messages() {
	}
}
