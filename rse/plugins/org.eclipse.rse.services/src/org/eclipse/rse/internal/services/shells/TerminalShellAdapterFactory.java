/*******************************************************************************
 * Copyright (c) 2008 MontaVista Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Anna Dushistova (MontaVista)- initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.services.shells;

import org.eclipse.core.runtime.IAdapterFactory;

import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.services.terminals.ITerminalService;

/**
 * @since 3.1
 */
public class TerminalShellAdapterFactory implements IAdapterFactory {

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		Object result = null;
		if (adaptableObject instanceof ITerminalService) {
			if (adapterType == IShellService.class) {
				result = new TerminalShellService(
						(ITerminalService) adaptableObject);
			}
		}
		return result;
	}

	public Class[] getAdapterList() {
		return new Class[] { IShellService.class };
	}

}
