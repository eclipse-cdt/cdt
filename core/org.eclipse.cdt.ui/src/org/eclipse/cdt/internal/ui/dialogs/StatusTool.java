/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.DialogPage;

public class StatusTool {
	/**
	 * Applies the status to the status line of a dialog page
	 */
	public static void applyToStatusLine(MessageLine messageLine, IStatus status) {
		String[] messages = getErrorMessages(status);
		messageLine.setErrorMessage(messages[0]);
		messageLine.setMessage(messages[1]);
	}

	/**
	 * Applies the status to the status line of a dialog page
	 */
	public static void applyToStatusLine(DialogPage page, IStatus status) {
		String[] messages = getErrorMessages(status);
		page.setErrorMessage(messages[0]);
		page.setMessage(messages[1]);
	}

	/**
	 * Returns error-message / warning-message for a status
	 */
	public static String[] getErrorMessages(IStatus status) {
		String message = status.getMessage();
		if (status.matches(IStatus.ERROR) && !"".equals(message)) { //$NON-NLS-1$
			return new String[] { message, null };
		} else if (status.matches(IStatus.WARNING | IStatus.INFO)) {
			return new String[] { null, message };
		} else {
			return new String[] { null, null };
		}
	}

	/**
	 * Compare two IStatus. The more severe is returned:
	 * An error is more severe than a warning, and a warning is more severe
	 * than ok.
	 */
	public static IStatus getMoreSevere(IStatus s1, IStatus s2) {
		if (s1.getSeverity() > s2.getSeverity()) {
			return s1;
		}
		return s2;
	}

	/**
	 * Finds the most severe status from a array of status
	 * An error is more severe than a warning, and a warning is more severe
	 * than ok.
	 */
	public static IStatus getMostSevere(IStatus[] status) {
		IStatus max = null;
		for (int i = 0; i < status.length; i++) {
			IStatus curr = status[i];
			if (curr.matches(IStatus.ERROR)) {
				return curr;
			}
			if (max == null || curr.getSeverity() > max.getSeverity()) {
				max = curr;
			}
		}
		return max;
	}
}
