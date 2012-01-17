/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (IRestartHandler.class.equals(adapterType)) {
			if (adaptableObject instanceof IRestart) {
				return fgRestartCommand;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	public Class[] getAdapterList() {
		return new Class[] {
				IRestartHandler.class
		};
	}

}
