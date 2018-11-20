/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service.command;

/**
 * Token returned by ICommandControl.queueCommand().  This token can be used
 * to uniquely identify a command when calling ICommandControl.removeCommand()
 * or when implementing the ICommandListener listener methods.
 *
 * @since 1.0
 */
public interface ICommandToken {
	/**
	 * Returns the command that this was created for.
	 */
	public ICommand<? extends ICommandResult> getCommand();
}