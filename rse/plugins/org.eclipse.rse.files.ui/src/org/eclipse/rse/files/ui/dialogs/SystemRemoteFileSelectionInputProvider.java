/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.files.ui.dialogs;

import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.ui.view.SystemResourceSelectionInputProvider;


public class SystemRemoteFileSelectionInputProvider extends
		SystemResourceSelectionInputProvider
{

	public SystemRemoteFileSelectionInputProvider(IHost connection)
	{
		super(connection);
		setCategory("files");
	}
	
	public SystemRemoteFileSelectionInputProvider()
	{
		super();
		setCategory("files");
	}

	protected ISubSystem getSubSystem(IHost selectedConnection)
	{
		return RemoteFileUtility.getFileSubSystem(selectedConnection);
	}

}