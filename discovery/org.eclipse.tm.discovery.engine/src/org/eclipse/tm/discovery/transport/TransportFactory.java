/********************************************************************************
 * Copyright (c) 2006, 2007 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 ********************************************************************************/

package org.eclipse.tm.discovery.transport;

import java.util.Vector;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

/**
 * Factory for the remoste system connection.
 */

public class TransportFactory {

	/*
	 * Extension point containing the transport implementations available in the workbench  
	 */
	private static IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.tm.discovery.engine","discoveryTransport"); //$NON-NLS-1$ //$NON-NLS-2$
	
	
	/**
	 * Returns the available transport names. The transport names are registered in the field <b>name</b> of the extension point <b>discoveryTransport</b> 
	 * 
	 * @return
	 * Array of available transport names 
	 */
	public static String[] getTransportList()
	{
		Vector transports = new Vector();
		
		IConfigurationElement[] ce = ep.getConfigurationElements();
		for (int i = 0; i < ce.length; i++) {
			String name = ce[i].getAttribute("name"); //$NON-NLS-1$
			if(name!=null)
				transports.add(name);
		}
		
		String[] transportsList = new String[transports.size()];
		transports.copyInto(transportsList);
		return transportsList;
	}
	
	/**
	 * Gets the ITransport implementation of a transport given its name
	 * 
	 * @param transportName
	 * Name of the transport 
	 * @param address
	 * Address of the target device
	 * @param timeOut
	 * Timeout in milliseconds
	 * @return
	 * ITranport implementation of the transport matching the given name. Null if the transport cannot be found.
	 * @throws Exception
	 */
	public static ITransport getTransport(String transportName, String address, int timeOut) throws Exception {
		
		ITransport transport = null;
		
		IConfigurationElement[] ce = ep.getConfigurationElements();
		for (int i = 0; i < ce.length; i++) {
			String name = ce[i].getAttribute("name"); //$NON-NLS-1$
			if(name!=null)
				if(name.equalsIgnoreCase(transportName))
				{
					transport = (ITransport)ce[i].createExecutableExtension("class"); //$NON-NLS-1$
					transport.setTargetAddress(address);
					transport.setTimeOut(timeOut);
					
				}
		}
		return transport;
	}

}
