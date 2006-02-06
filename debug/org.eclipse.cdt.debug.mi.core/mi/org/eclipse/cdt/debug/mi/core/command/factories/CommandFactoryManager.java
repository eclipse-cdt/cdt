/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.command.factories; 

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.cdt.debug.mi.core.MIPlugin;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
 
/**
 * Manages command factories.
 */
public class CommandFactoryManager {

	private List fDescriptors = null;

	public CommandFactoryDescriptor[] getDescriptors() {
		List factories = getDescriptorList();
		return (CommandFactoryDescriptor[])factories.toArray( new CommandFactoryDescriptor[factories.size()] );
	}

	public CommandFactoryDescriptor getDefaultDescriptor( String debuggerID ) {
		// TODO: temporary
		CommandFactoryDescriptor[] descriptors = getDescriptors( debuggerID );
		return descriptors[0];
	}

	public CommandFactoryDescriptor[] getDescriptors( String debuggerID ) {
		String platform = Platform.getOS();
		List all = getDescriptorList();
		ArrayList list = new ArrayList( all.size() );
		Iterator it = all.iterator();
		while( it.hasNext() ) {
			CommandFactoryDescriptor desc = (CommandFactoryDescriptor)it.next();
			if ( desc.getDebuggerIdentifier().equals( debuggerID ) && desc.supportsPlatform( platform ) ) {
				list.add( desc );
			}
		}
		return (CommandFactoryDescriptor[])list.toArray( new CommandFactoryDescriptor[list.size()] );
	}

	public CommandFactory getCommandFactory( String factoryID ) throws CoreException {
		List all = getDescriptorList();
		Iterator it = all.iterator();
		while( it.hasNext() ) {
			CommandFactoryDescriptor desc = (CommandFactoryDescriptor)it.next();
			if ( desc.getIdentifier().equals( factoryID ) ) {
				return desc.getCommandFactory();
			}
		}
		return null;
	}

	private List getDescriptorList() {
		if ( fDescriptors == null )
			initializeDescriptorList();
		return fDescriptors;
	}

	private synchronized void initializeDescriptorList() {
		if ( fDescriptors == null ) {
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint( MIPlugin.getUniqueIdentifier(), MIPlugin.EXTENSION_POINT_COMMAND_FACTORIES );
			IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
			fDescriptors = new ArrayList( infos.length );
			for( int i = 0; i < infos.length; i++ ) {
				IConfigurationElement configurationElement = infos[i];
				CommandFactoryDescriptor factory = new CommandFactoryDescriptor( configurationElement );
				fDescriptors.add( factory );
			}
		}
	}
}
