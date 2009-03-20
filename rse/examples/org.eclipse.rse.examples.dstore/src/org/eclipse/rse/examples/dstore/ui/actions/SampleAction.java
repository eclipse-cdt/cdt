/********************************************************************************
 * Copyright (c) 2009 IBM Corporation. All rights reserved.
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
package org.eclipse.rse.examples.dstore.ui.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.examples.dstore.subsystems.RemoteSampleObject;
import org.eclipse.rse.examples.dstore.subsystems.SampleSubSystem;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.swt.widgets.Shell;

public class SampleAction extends SystemBaseAction {

	public class SampleJob extends Job {
		private RemoteSampleObject _selectedObject;
		public SampleJob(RemoteSampleObject selectedObject){
			super("Sample Job");
			_selectedObject = selectedObject;
		}
		
		public IStatus run(IProgressMonitor monitor){
			SampleSubSystem ss = (SampleSubSystem)_selectedObject.getSubSystem();

			// do remote action
			ss.doRemoteAction(_selectedObject, monitor);
			
			// refresh view
			ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
			sr.fireEvent(new SystemResourceChangeEvent(_selectedObject.getParent(), ISystemResourceChangeEvents.EVENT_REFRESH, _selectedObject.getParent()));
			return Status.OK_STATUS;
		}
	}
	

	
	public SampleAction(Shell shell) {
		super("Do Remote Operation", shell);
	}

	@Override
	public void run() {
		
		RemoteSampleObject obj = (RemoteSampleObject)getSelection().getFirstElement();
		if (obj != null){
			SampleJob job = new SampleJob(obj);
			job.schedule();
		}
	}



	
}
