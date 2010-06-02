/*******************************************************************************
 * Copyright (c) 2009, 2010 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.dialogs;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * The IInputStatusValidator is the interface for IStatus validators. 
 * @since 5.2
 */
public interface IInputStatusValidator {
	/**
	 * Validates the given string. Returns the status with an error/warning/info message to display if the new
	 * text fails validation.
	 * 
	 * @param newText
	 *            the text to check for validity
	 * 
	 * @return {@link IStatus} object. For the purpose of validation severity and message are considered.
	 *         <li/>{@link Status#OK_STATUS} or any {@link IStatus#OK} to indicate no error.
	 *         <li/>{@link IStatus#ERROR} indicates an error.
	 *         <li/>{@link IStatus#WARNING} indicates a warning.
	 *         <li/>{@link IStatus#INFO} indicates an informational message.
	 */
	public IStatus isValid(String newText);
}
