/********************************************************************************
 * Copyright (c) 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * David McKnight   (IBM)        - [175308] Need to use a job to wait for shell to exit
 ********************************************************************************/
package org.eclipse.rse.internal.subsystems.processes.shell.linux;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

class WaiterJob extends Job
{
	private Process _p;
	public WaiterJob(Process p)
	{
		super("LinuxShellProcessWaiter"); //$NON-NLS-1$
		_p = p;
		setSystem(true);
	}
	
	public IStatus run(IProgressMonitor monitor) {
        try {
           _p.waitFor();
           if (_p.exitValue()!=0) 
           {
               String errMsg = Activator.getErrorMessage(_p.getErrorStream());
               if (!errMsg.trim().equals("")) { //$NON-NLS-1$
                   Activator.logErrorMessage(errMsg.toString());
               }
           }
        } catch(InterruptedException e) { 
           return Status.CANCEL_STATUS;
        }
        return Status.OK_STATUS;
     }
}