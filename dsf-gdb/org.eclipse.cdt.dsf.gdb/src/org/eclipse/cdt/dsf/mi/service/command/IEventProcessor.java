/*******************************************************************************
 * Copyright (c) 2012 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command;

import org.eclipse.cdt.dsf.debug.service.command.ICommandListener;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;

/**
 * Common interface that represents all MI and CLI event/command processors.
 *
 * @since 4.1
 */
public interface IEventProcessor extends IEventListener, ICommandListener {

	/**
	 * Disposes of this processor's resources.
	 */
	public void dispose();
}
