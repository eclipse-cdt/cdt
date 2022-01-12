/*******************************************************************************
 *  Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.correction;

/**
 * Correction proposals implement this interface to by invokable by a command.
 * (e.g. keyboard shortcut)
 */
public interface ICommandAccess {

	/**
	 * Returns the id of the command that should invoke this correction proposal
	 * @return the id of the command. This id must start with {@link CorrectionCommandInstaller#COMMAND_PREFIX}
	 * to be recognizes as correction command.
	 */
	String getCommandId();
}
