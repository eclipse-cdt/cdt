/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
