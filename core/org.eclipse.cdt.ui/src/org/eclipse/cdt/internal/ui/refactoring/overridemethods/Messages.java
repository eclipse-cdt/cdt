/*******************************************************************************
 * Copyright (c) 2017 Pavel Marek
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Pavel Marek - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.overridemethods;

import org.eclipse.osgi.util.NLS;

final class Messages extends NLS {
	public static String OverrideMethodsInputPage_Name;
	public static String OverrideMethodsInputPage_Header;
	public static String OverrideMethodsInputPage_SelectAll;
	public static String OverrideMethodsInputPage_DeselectAll;
	public static String OverrideMethodsRefactoring_SelNotInClass;
	public static String OverrideMethodsRefactoring_NoMethods;
	public static String OverrideMethodsRefactoring_PreserveVirtual;
	public static String OverrideMethodsRefactoring_AddOverride;
	public static String OverrideMethodsRefactoring_LinkDescription;
	public static String OverrideMethodsRefactoring_LinkTooltip;
	public static String OverrideMethods_label;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	// Do not instantiate
	private Messages() {
	}
}
