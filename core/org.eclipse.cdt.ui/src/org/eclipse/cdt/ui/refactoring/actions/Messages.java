/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
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
 *******************************************************************************/
package org.eclipse.cdt.ui.refactoring.actions;

import org.eclipse.osgi.util.NLS;

class Messages extends NLS {
	public static String CRefactoringActionGroup_menu;
	public static String CRenameAction_label;
	public static String ExtractConstantAction_label;
	public static String ExtractLocalVariableAction_label;
	public static String ExtractFunctionAction_label;
	public static String HideMethodAction_label;
	public static String ImplementMethodAction_label;
	public static String GettersAndSetters_label;
	public static String ToggleFunctionAction_label;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	// Do not instantiate
	private Messages() {
	}
}
