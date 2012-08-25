/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.hidemethod;

import org.eclipse.osgi.util.NLS;

final class Messages extends NLS {
	public static String HideMethodRefactoring_HasExternalReferences;
	public static String HideMethodRefactoring_HIDE_METHOD;
	public static String HideMethodRefactoring_NoNameSelected;
	public static String HideMethodRefactoring_NoMethodNameSelected;
	public static String HideMethodRefactoring_CanOnlyHideMethods;
	public static String HideMethodRefactoring_FILE_CHANGE_TEXT;
	public static String HideMethodRefactoring_EnclosingClassNotFound;
	public static String HideMethodRefactoring_IsAlreadyPrivate;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	// Do not instantiate
	private Messages() {
	}
}
