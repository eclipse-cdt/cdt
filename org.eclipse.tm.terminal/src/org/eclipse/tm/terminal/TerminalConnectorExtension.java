/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.terminal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;

/**
 * A factory to get {@link ITerminalConnector} instances.
 * 
 * @author Michael Scharf
 *
 */
public class TerminalConnectorExtension {
	/**
	 * @return a new list of ITerminalConnectors. 
	 */
	public static ITerminalConnector[] getTerminalConnectors() {
		IConfigurationElement[] config=RegistryFactory.getRegistry().getConfigurationElementsFor("org.eclipse.tm.terminal.terminalConnector"); //$NON-NLS-1$
		List result=new ArrayList();
		for (int i = 0; i < config.length; i++) {
			try {
				Object obj=config[i].createExecutableExtension("class"); //$NON-NLS-1$
				if(obj instanceof ITerminalConnector) {
					ITerminalConnector conn=(ITerminalConnector) obj;
					if(conn.isInstalled())
						result.add(conn);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return (ITerminalConnector[]) result.toArray(new ITerminalConnector[result.size()]);
	}
}
