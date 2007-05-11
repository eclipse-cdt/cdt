/********************************************************************************
 * Copyright (c) 2006, 2007 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Javier Montalvo Orus (Symbian) - initial API and implementation
 ********************************************************************************/

package org.eclipse.tm.discovery.protocol;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

/**
 * Static factory to handle the protocol implementations.</br> 
 */

public class ProtocolFactory {

	
	/*
	 * Extension point containing the protocol implementations available in the workbench  
	 */
	private static IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.tm.discovery.engine","discoveryProtocol"); //$NON-NLS-1$ //$NON-NLS-2$
	
	
	/**
	 * Returns the available protocol names. This protocol names are registered in the field <b>name</b> of the extension point <b>discoveryProtocol</b> 
	 * 
	 * @return
	 * Array of available protocol names 
	 */
	public static String[] getProtocolList()
	{
		Vector protocols = new Vector();
		
		IConfigurationElement[] ce = ep.getConfigurationElements();
		for (int i = 0; i < ce.length; i++) {
			String name = ce[i].getAttribute("name"); //$NON-NLS-1$
			if(name!=null)
				protocols.add(name);
		}
		
		String[] protocolsList = new String[protocols.size()];
		protocols.copyInto(protocolsList);
		return protocolsList;
	}
	
	/**
	 * Returns the recommended queries for the specified protocol 
	 * 
	 * @param protocolName
	 * Name of the protocol 
	 * @return
	 * Array of recommended queries to be send using this protocol.
	 * @throws CoreException
	 */
	public static String[] getQueryList(String protocolName) throws CoreException
	{
		IProtocol protocol = null;
		String[] queries = null;
		
		IConfigurationElement[] ce = ep.getConfigurationElements();
		for (int i = 0; i < ce.length; i++) {
			String name = ce[i].getAttribute("name"); //$NON-NLS-1$
			if(name!=null)
				if(name.equalsIgnoreCase(protocolName))
				{
					protocol = (IProtocol)ce[i].createExecutableExtension("class"); //$NON-NLS-1$
				}
		}
		
		if(protocol != null)
		{	
			queries = protocol.getQueries();
			if(queries==null)
				queries = new String[]{};
		}
		return queries;
		
	}
	
	/**
	 * Gets the IProtocol implementation of a protocol given its name
	 * 
	 * @param protocolName
	 * Name of the protocol 
	 * @return
	 * IProtocol implementation of the protocol matching the given name. Null if the protocol cannot be found.
	 * @throws CoreException
	 * 
	 * @see IProtocol
	 */
	public static IProtocol getProtocol(String protocolName) throws CoreException {
		
		IProtocol protocol = null;
		
		IConfigurationElement[] ce = ep.getConfigurationElements();
		for (int i = 0; i < ce.length; i++) {
			String name = ce[i].getAttribute("name"); //$NON-NLS-1$
			if(name!=null)
				if(name.equalsIgnoreCase(protocolName))
				{
					protocol = (IProtocol)ce[i].createExecutableExtension("class"); //$NON-NLS-1$
				}
		}
		return protocol;
	}
	
	/**
	 * Gets the multicast address given a protocol name and a transport name or returns null if this information is not available
	 * 
	 * @param protocolName
	 * Name of the protocol 
	 * @param transportName
	 * Name of the transport
	 * @return
	 * String representing the multicast address of the given protocol and transport or null if not available
	 * @throws CoreException
	 * 
	 * @see IProtocol
	 */
	public static String getMulticastAddress(String protocolName, String transportName) throws CoreException {
		
		String multiCastAddress = null;
		
		IConfigurationElement[] ce = ep.getConfigurationElements();
		for (int i = 0; i < ce.length; i++) {
			String name = ce[i].getAttribute("name"); //$NON-NLS-1$
			if(name!=null)
				if(name.equalsIgnoreCase(protocolName))
				{
					String multicastAddresses = ce[i].getAttribute("multicast"); //$NON-NLS-1$
					if(multicastAddresses==null)
						break;
					
					String[] pairs = multicastAddresses.split(";"); //$NON-NLS-1$
					for (int j = 0; j < pairs.length; j++) {
						
						URI uri=null;
						try {
							uri = new URI(pairs[j]);
						} catch (URISyntaxException e) {}
						
						if(uri!=null)
						{
							if(uri.getScheme().equals(transportName))
							{
								multiCastAddress = uri.getSchemeSpecificPart();
								break;
							}
						}
					}
				}
		}
		return multiCastAddress;
	}

}
