/********************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * Anna Dushistova  (MontaVista) - [239159] The shell process subsystem not working without the shells subsystem present for the systemType
 ********************************************************************************/
package org.eclipse.rse.internal.subsystems.processes.shell.linux;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.AbstractDelegatingConnectorService;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.subsystems.processes.servicesubsystem.IProcessServiceSubSystem;

/**
 * This class delegates the connector service requests for the linux process 
 * subsystem to the connector service of the shell subsystem.
 */
public class DelegatingShellProcessConnectorService extends AbstractDelegatingConnectorService 
{
	private IConnectorService _realService;

	/**
	 * @param host the linux host that is the target for this connector service.
	 */
	public DelegatingShellProcessConnectorService(IHost host) 
	{
		super(host);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractDelegatingConnectorService#getRealConnectorService()
	 */
	public IConnectorService getRealConnectorService()
	{
		if (_realService != null)
		{
			return _realService;
		}
		else
		{
			ISubSystem ss = Activator.getSuitableSubSystem(getHost());
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
