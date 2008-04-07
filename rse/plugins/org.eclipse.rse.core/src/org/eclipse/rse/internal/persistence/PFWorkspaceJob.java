/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * David Dykstal (IBM) - [188863] created out of SaveRSEDOMJob
 * David McKnight   (IBM)        - [216252] MessageFormat.format -> NLS.bind
 *******************************************************************************/

package org.eclipse.rse.internal.persistence;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.internal.core.RSECoreMessages;
import org.eclipse.rse.persistence.IRSEPersistenceProvider;
import org.eclipse.rse.persistence.dom.RSEDOM;

/**
 * The PFWorkspaceJob is a workspace job that belongs to the family 
 * {@link RSECorePlugin#getThePersistenceManager()}. It is used to 
 * save a DOM to the workspace. A DOM corresponds to a profile.
 */
public class PFWorkspaceJob extends WorkspaceJob {

	private RSEDOM _dom;
	private IRSEPersistenceProvider _provider;
	
	public PFWorkspaceJob(RSEDOM dom, IRSEPersistenceProvider provider) {
		super("Saving Profile"); //$NON-NLS-1$
		String title = NLS.bind(RSECoreMessages.SaveRSEDOMJob_SavingProfileJobName, dom.getName());
		setName(title);
		setRule(ResourcesPlugin.getWorkspace().getRoot());
		_dom = dom;
		_provider = provider;
	}

	public IStatus runInWorkspace(IProgressMonitor monitor) {
		IStatus result = Status.OK_STATUS;
		boolean saved = _provider.saveRSEDOM(_dom, monitor);
		if (!saved) {
			result = Status.CANCEL_STATUS;
		}
		return result;
	}
	
	public boolean belongsTo(Object family) {
		Object[] families = new Object[] {RSECorePlugin.getThePersistenceManager()};
		for (int i = 0; i < families.length; i++) {
			Object object = families[i];
			if (family == object) return true;
		}
		return super.belongsTo(family);
	}

}
