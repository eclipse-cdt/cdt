/*******************************************************************************
 *  Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      IBM - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.implementmethod;

import org.eclipse.osgi.util.NLS;

final class Messages extends NLS {
	public static String ParameterNamesInputPage_Title;
	public static String ParameterNamesInputPage_CompleteMissingMails;
	public static String ImplementMethodInputPage_PageTitle;
	public static String ImplementMethodInputPage_SelectAll;
	public static String ImplementMethodInputPage_DeselectAll;
	public static String ImplementMethodRefactoringPage_GeneratingPreview;
	public static String ImplementMethodRefactoringPage_PreviewCanceled;
	public static String ImplementMethodRefactoringPage_PreviewGenerationNotPossible;
	public static String ImplementMethodRefactoring_NoMethodToImplement;
	public static String ImplementMethodRefactoring_MethodHasImpl;
	public static String ImplementMethodRefactoring_NoImplFile;
	public static String ImplementMethodRefactoringWizard_CancelingPreviewGeneration;
	public static String ImplementMethodInputPage_Header;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	// Do not instantiate
	private Messages() {
	}
}
