/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.refactoring.actions;

import org.eclipse.osgi.util.NLS;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.ui.refactoring.actions.messages"; //$NON-NLS-1$

	public static String CRefactoringActionGroup_menu;
	public static String CRenameAction_label;
	public static String ExtractConstantAction_label;
	/**
	 * @since 5.1
	 */
	public static String ExtractLocalVariableAction_label;
	public static String ExtractFunctionAction_label;
	public static String HideMethodAction_label;
	public static String ImplementMethodAction_label;
	public static String GettersAndSetters_label;
	/**
	 * @since 5.3
	 */
	public static String ToggleFunctionAction_label;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
