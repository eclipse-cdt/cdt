/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.inlinelocalvariable;

import org.eclipse.osgi.util.NLS;

final class Messages extends NLS {
	public static String InlineLocalVariable;
	public static String NoExpressionSelected;
	public static String TooManyExpressionsSelected;
	public static String NoIdExpressionSelected;
	public static String VariableUsedOutside;
	public static String FieldReference;
	public static String DefaultInitialized;
	public static String Uninitialized;
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	// Do not instantiate
	private Messages() {
	}
}
