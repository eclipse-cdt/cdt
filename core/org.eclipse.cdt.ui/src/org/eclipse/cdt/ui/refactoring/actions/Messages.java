/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
