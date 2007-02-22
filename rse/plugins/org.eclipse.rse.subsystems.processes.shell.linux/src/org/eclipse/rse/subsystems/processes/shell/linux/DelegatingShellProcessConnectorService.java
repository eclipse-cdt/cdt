/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.subsystems.processes.shell.linux;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.AbstractDelegatingConnectorService;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.subsystems.processes.shell.linux.Activator;
import org.eclipse.rse.subsystems.processes.servicesubsystem.IProcessServiceSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IShellServiceSubSystem;

public class DelegatingShellProcessConnectorService extends AbstractDelegatingConnectorService 
{
	private IConnectorService _realService;
	public DelegatingShellProcessConnectorService(IHost host) 
	{
		super(host);
	}

	public IConnectorService getRealConnectorService()
	{
		if (_realService != null)
		{
			return _realService;
		}
		else
		{
			IShellServiceSubSystem ss = Activator.getShellServiceSubSystem(getHost());
			if (ss != null)
			{
				_realService = ss.getConnectorService();
				
				// register the process subsystem
				IProcessServiceSubSystem ps = Activator.getProcessServiceSubSystem(getHost());
				_realService.registerSubSystem(ps);
				return _realService;
			}
			else
			{
				return null;
			}
		}
	}

}
