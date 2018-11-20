/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.util;

import org.eclipse.cdt.ui.CUIPlugin;

public class Util implements IDebugLogConstants {
	public static boolean VERBOSE_CONTENTASSIST = false;

	private Util() {
	}

	/**
	 * Adds a log entry
	 */
	public static void debugLog(String message, DebugLogConstant client) {
		if (CUIPlugin.getDefault() == null)
			return;
		if (CUIPlugin.getDefault().isDebugging() && isActive(client)) {
			while (message.length() > 100) {
				String partial = message.substring(0, 100);
				message = message.substring(100);
				System.out.println(partial + "\\"); //$NON-NLS-1$
			}
			if (message.endsWith("\n")) { //$NON-NLS-1$
				System.err.print(message);
			} else {
				System.out.println(message);
			}
		}
	}

	public static boolean isActive(DebugLogConstant client) {
		if (client.equals(IDebugLogConstants.CONTENTASSIST)) {
			return VERBOSE_CONTENTASSIST;
		}
		return false;
	}
}
