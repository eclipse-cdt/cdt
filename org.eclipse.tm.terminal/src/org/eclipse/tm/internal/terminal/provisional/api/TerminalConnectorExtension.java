/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.provisional.api;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.tm.internal.terminal.connector.TerminalConnector;

/**
 * A factory to get {@link ITerminalConnector} instances.
 * 
 * @author Michael Scharf
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * work or that it will remain the same. Please do not use this API without
 * consulting with the <a href="http://www.eclipse.org/dsdp/tm/">Target Management</a> team.
 * </p>
 */
public class TerminalConnectorExtension {
	static private ITerminalConnector makeConnector(final IConfigurationElement config) {
		String id = config.getAttribute("id"); //$NON-NLS-1$
		if(id==null || id.length()==0)
			id=config.getAttribute("class"); //$NON-NLS-1$
		String name= config.getAttribute("name"); //$NON-NLS-1$
		if(name==null || name.length()==0) {
			name=id;
		}
		TerminalConnector.Factory factory=new TerminalConnector.Factory(){
			public TerminalConnectorImpl makeConnector() throws Exception {
				return (TerminalConnectorImpl)config.createExecutableExtension("class"); //$NON-NLS-1$
			}};
		return new TerminalConnector(factory,id,name);
	}

	/**
	 * @param id the id of the terminal connector in the
	 * <code>org.eclipse.tm.terminal.terminalConnector</code> extension point
	 * @return a new ITerminalConnector with id or <code>null</code> if there is no
	 * extension with that id.
	 */
	public static ITerminalConnector makeTerminalConnector(String id) {
		IConfigurationElement[] config=RegistryFactory.getRegistry().getConfigurationElementsFor("org.eclipse.tm.terminal.terminalConnector"); //$NON-NLS-1$
		for (int i = 0; i < config.length; i++) {
			if(id.equals(config[i].getAttribute("id"))) { //$NON-NLS-1$
				return makeConnector(config[i]);
			}
		}
		return null;	
	}
	/**
	 * @return a new list of {@link ITerminalConnector} instances defined in 
	 * the <code>org.eclipse.tm.terminal.terminalConnector</code> extension point
	 */
	public static ITerminalConnector[] makeTerminalConnectors() {
		IConfigurationElement[] config=RegistryFactory.getRegistry().getConfigurationElementsFor("org.eclipse.tm.terminal.terminalConnector"); //$NON-NLS-1$
		List result=new ArrayList();
		for (int i = 0; i < config.length; i++) {
			result.add(makeConnector(config[i]));
		}
		return (ITerminalConnector[]) result.toArray(new ITerminalConnector[result.size()]);
	}

}
