/*******************************************************************************
 *  Copyright (c) 2005, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *    IBM - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.refactoring.implementmethod;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.refactoring.implementmethod.messages";//$NON-NLS-1$

	private Messages() {
		// Do not instantiate
	}

	public static String ParameterNamesInputPage_Title;
	public static String ParameterNamesInputPage_CompleteMissingMails;
	public static String PreviewGenerationNotPossible;
	public static String ImplementMethodInputPage_PageTitle;
	public static String ImplementMethodInputPage_SelectAll;
	public static String ImplementMethodInputPage_DeselectAll;
	public static String ImplementMethodRefactoringPage_GeneratingPreview;
	public static String ImplementMethodRefactoring_NoMethodSelected;
	public static String ImplementMethodRefactoring_MethodHasImpl;
	public static String ImplementMethodInputPage_Header;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
