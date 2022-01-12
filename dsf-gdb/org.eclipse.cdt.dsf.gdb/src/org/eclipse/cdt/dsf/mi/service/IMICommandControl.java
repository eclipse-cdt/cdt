/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;

/**
 * This interface provides a method for accessing the command factory.
 * @since 3.0
 */
public interface IMICommandControl extends ICommandControlService {
	/**
	 * Returns a command factory that creates different<code>ICommand</code>
	 * to be sent to the backend.  This factory can easily be overridden
	 * to specialize certain commands.
	 */
	public CommandFactory getCommandFactory();
}
