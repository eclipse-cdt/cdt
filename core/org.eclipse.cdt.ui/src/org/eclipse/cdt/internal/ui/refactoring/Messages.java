/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
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

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.refactoring.messages";//$NON-NLS-1$

	private Messages() {
		// Do not instantiate
	}

	public static String DeleteFileChange_0;
	public static String DeleteFileChange_1;
	public static String HSRRefactoring_name;
	public static String HSRRefactoring_PM_LoadTU;
	public static String HSRRefactoring_PM_CheckTU;
	public static String HSRRefactoring_PM_InitRef;
	public static String HSRRefactoring_PM_ParseTU;
	public static String HSRRefactoring_PM_MergeComments;
	public static String HSRRefactoring_CanceledByUser;
	public static String HSRRefactoring_CompileErrorInTU;
	public static String AddDeclarationNodeToClassChange_AddDeclaration;
	public static String CreateFileChange_CreateFile;
	public static String CreateFileChange_UnknownLoc;
	public static String CreateFileChange_FileExists;
	public static String HSRRefactoring_SelectionNotValid;
	public static String HSRRefactoring_CantLoadTU;
	public static String HSRRefactoring_Ambiguity;
	public static String NodeContainer_Name;
	public static String NodeContainer_Space;
	public static String NO_FILE;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
