/*******************************************************************************
 * Copyright (c) 2006, 2016 IBM Corporation and others.
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
 *     Wind River Systems - copied to use in CDT
 *******************************************************************************/
package org.eclipse.cdt.debug.core.command;

import org.eclipse.cdt.debug.core.model.IRestart;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.commands.IRestartHandler;

/**
 * Adapter factory for debug commands.
 *
 * @see org.eclipse.debug.core.command
 *
 * @since 7.0
 *
 */
public class CCommandAdapterFactory implements IAdapterFactory {
	private static IRestartHandler fgRestartCommand = new RestartCommand();

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (IRestartHandler.class.equals(adapterType)) {
			if (adaptableObject instanceof IRestart) {
				return (T) fgRestartCommand;
			}
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] { IRestartHandler.class };
	}

}
