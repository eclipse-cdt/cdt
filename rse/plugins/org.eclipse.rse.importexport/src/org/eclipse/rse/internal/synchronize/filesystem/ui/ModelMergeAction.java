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
package org.eclipse.rse.internal.synchronize.filesystem.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.internal.synchronize.RSESyncUtils;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.core.subscribers.SubscriberScopeManager;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.mapping.ITeamContentProviderManager;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ModelParticipantAction;

public class ModelMergeAction extends ModelParticipantAction {

	public ModelMergeAction(String text,
			ISynchronizePageConfiguration configuration) {
		super(text, configuration);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean isEnabledForSelection(IStructuredSelection selection) {
		// Only enable the put in outgoing or both modes
		int mode = getConfiguration().getMode();
		if (mode == ISynchronizePageConfiguration.OUTGOING_MODE || mode == ISynchronizePageConfiguration.BOTH_MODE) {
			return getResourceMappings(selection).length > 0;
		}
		return false;
	}
	
	private ResourceMapping[] getResourceMappings(IStructuredSelection selection) {
		List mappings = new ArrayList();
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object element = iter.next();
			ResourceMapping mapping = Utils.getResourceMapping(element);
			if (mapping != null)
				mappings.add(mapping);
		}
		return (ResourceMapping[]) mappings.toArray(new ResourceMapping[mappings.size()]);
	}
	
	private IMergeContext getContext(ResourceMapping[] mappings) {
		//SubscriberScopeManager manager = FileSystemOperation.createScopeManager(FileSystemSubscriber.getInstance().getName(), mappings);
		//return new FileSystemMergeContext(manager);
		
		return ((IMergeContext)getConfiguration().getProperty(ITeamContentProviderManager.P_SYNCHRONIZATION_CONTEXT));
	}
	
	public void run() {
		ResourceMapping[] resourceMappings = getResourceMappings(getStructuredSelection());
		SubscriberScopeManager manager = FileSystemOperation.createScopeManager("Merge", resourceMappings);
		try {
			new MergeOperation(getConfiguration(), resourceMappings, getContext(resourceMappings)).run();
		} catch (InvocationTargetException e) {
			IStatus status = getStatus(e);
			ErrorDialog.openError(getConfiguration().getSite().getShell(), null, null, status);
		} catch (InterruptedException e) {
			// Ignore
		}
	}
	
	private IStatus getStatus(Throwable throwable) {
		if (throwable instanceof InvocationTargetException) {
			return getStatus(((InvocationTargetException) throwable).getCause());
		}
		return new Status(IStatus.ERROR, RSESyncUtils.PLUGIN_ID, 0, "An error occurred during the put.", throwable);
	}
}
