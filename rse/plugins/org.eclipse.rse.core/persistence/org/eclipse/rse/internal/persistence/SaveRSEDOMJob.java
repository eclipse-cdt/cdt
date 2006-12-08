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

package org.eclipse.rse.internal.persistence;

import java.text.MessageFormat;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.persistence.IRSEPersistenceProvider;
import org.eclipse.rse.persistence.dom.RSEDOM;

public class SaveRSEDOMJob extends WorkspaceJob {
	
	private RSEDOM _dom;
	private IRSEPersistenceProvider _provider;

	public SaveRSEDOMJob(RSEDOM dom, IRSEPersistenceProvider provider) {
		super("Saving Profile"); //$NON-NLS-1$
		String title = MessageFormat.format(Messages.SaveRSEDOMJob_SavingProfileJobName, new Object[] {dom.getName()});
		setName(title);
		_dom = dom;
		_provider = provider;
	}

	public IStatus runInWorkspace(IProgressMonitor monitor) {
		IStatus result = Status.OK_STATUS;
		synchronized (_dom) { // synchronize on the DOM to prevent its update while writing
			if (_dom.needsSave()) {
				_provider.saveRSEDOM(_dom, monitor);
				_dom.markUpdated();
			} else {
				result = Status.CANCEL_STATUS;
			}
		}
		return result;
	}

}