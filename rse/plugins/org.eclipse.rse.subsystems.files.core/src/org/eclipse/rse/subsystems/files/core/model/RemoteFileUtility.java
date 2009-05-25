/********************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [220020][api][breaking] SystemFileTransferModeRegistry should be internal
 * David McKnight   (IBM)        - [267247] Wrong encoding - new method to get source encoding for IFile
 ********************************************************************************/

package org.eclipse.rse.subsystems.files.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.subsystems.files.core.model.SystemFileTransferModeRegistry;
import org.eclipse.rse.services.clientserver.SystemEncodingUtil;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration;


/**
 * Public utility class for dealing with remote file subsystems.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class RemoteFileUtility
{
	/**
	 * Return the workspace encoding for a given IFile.  In none is specified
	 * for this particular file, then the default charset is used.  If an exception
	 * is hit trying to get the encoding, then the fallback is UTF-8
	 * 
	 * @since 3.1
	 */
	public static String getSourceEncoding(IFile file)
	{
		String srcEncoding = null;
		
		try {
			srcEncoding = file.getCharset(true);
			if (srcEncoding == null || srcEncoding.length() == 0)
			{
				srcEncoding = file.getWorkspace().getRoot().getDefaultCharset();
			}
		}
		catch (CoreException e){			
			srcEncoding = SystemEncodingUtil.ENCODING_UTF_8;
		}
		return srcEncoding;
	}
	
	/**
	 * Return the first remote file subsystem associated with a connection.
	 * @param connection the connection to query.
	 * @return an IRemoteFileSubSystem instance, or <code>null</code> if
	 *     no file subsystem is configured with the given connection.
	 */
	public static IRemoteFileSubSystem getFileSubSystem(IHost connection)
	{
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		ISubSystem[] sses = sr.getSubSystems(connection);
		for (int i = 0; i < sses.length; i++)
		{
			if (sses[i] instanceof IRemoteFileSubSystem)
			{
				IRemoteFileSubSystem subSystem = (IRemoteFileSubSystem)sses[i];
				return subSystem;
			}
		}
		return null;
	}

	/**
	 * Return the list of file subsystems associated with a connection.
	 * @param connection the connection to query.
	 * @return a list of IRemoteFileSubSystem instances (may be empty).
	 */
	public static IRemoteFileSubSystem[] getFileSubSystems(IHost connection)
	{
		List results = new ArrayList();
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		ISubSystem[] sses = sr.getSubSystems(connection);
		for (int i = 0; i < sses.length; i++)
		{
			if (sses[i] instanceof IRemoteFileSubSystem)
			{
				IRemoteFileSubSystem subSystem = (IRemoteFileSubSystem)sses[i];
				results.add(subSystem);
			}
		}
		return (IRemoteFileSubSystem[])results.toArray(new IRemoteFileSubSystem[results.size()]);
	}

	/**
	 * Return the first remote file subsystem configuration associated with a system type.
	 * @param systemType the system type to query.
	 * @return an IRemoteFileSubSystemConfiguration instance, or <code>null</code> if
	 *     no file subsystem is configured with the given system type.
	 */
	public static IRemoteFileSubSystemConfiguration getFileSubSystemConfiguration(IRSESystemType systemType)
	{
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		ISubSystemConfiguration[] sses = sr.getSubSystemConfigurationsBySystemType(systemType, false);
		for (int i = 0; i < sses.length; i++)
		{
			if (sses[i] instanceof IRemoteFileSubSystemConfiguration)
			{
				return (IRemoteFileSubSystemConfiguration)sses[i];
			}
		}
		return null;
	}

	/**
	 * Return the global SystemFileTransferModeRegistry.
	 * @return the global SystemFileTransferModeRegistry.
	 * @since 3.0
	 */
	public static ISystemFileTransferModeRegistry getSystemFileTransferModeRegistry()
	{
		return SystemFileTransferModeRegistry.getInstance();
	}

}