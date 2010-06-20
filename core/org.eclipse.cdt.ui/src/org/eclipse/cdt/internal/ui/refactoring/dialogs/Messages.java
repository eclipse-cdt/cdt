/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *    Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.dialogs;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

	public static String CTextEditChangePreviewViewer_OrgSource;
	public static String CTextEditChangePreviewViewer_RefactoredSource;
	public static String ExtractInputPage_ReplaceInSubclass;
	public static String ExtractInputPage_EnterName;
	public static String ExtractInputPage_CheckName;
	public static String VisibilitySelectionPanel_AccessModifier;
	public static String ValidatingLabeledTextField_CantBeEmpty;
	public static String ValidatingLabeledTextField_InvalidCharacters;
	public static String ValidatingLabeledTextField_DuplicatedNames;
	public static String ValidatingLabeledTextField_IsKeyword;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	// Do not instantiate
	private Messages() {
	}
}
