/*******************************************************************************
 * Copyright (c) 2006, 2020 Wind River Systems, Inc. and others.
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
 *     Sergey Prigogin (Google)
 *     Alexander Fedorov (ArSysOp) - Bug 561993 - Remove dependency to com.ibm.icu from CDT UI
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.util;

import java.text.MessageFormat;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String EditorUtility_calculatingChangedRegions_message;
	public static String EditorUtility_error_calculatingChangedRegions;

	public static String OpenExternalProblemAction_CannotReadExternalLocation;

	public static String OpenExternalProblemAction_ErrorOpeningFile;

	public static String format(String pattern, Object... arguments) {
		return MessageFormat.format(pattern, arguments);
	}

	private Messages() {
		// Do not instantiate
	}

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
